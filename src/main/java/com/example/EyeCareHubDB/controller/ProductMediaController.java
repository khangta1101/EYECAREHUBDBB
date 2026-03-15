package com.example.EyeCareHubDB.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.EyeCareHubDB.dto.ProductMediaCreateRequest;
import com.example.EyeCareHubDB.dto.ProductMediaDTO;
import com.example.EyeCareHubDB.dto.ProductMediaUpdateRequest;
import com.example.EyeCareHubDB.service.ProductMediaService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Product Media", description = "Product Media Management APIs")
public class ProductMediaController {
    
    private final ProductMediaService mediaService;
    private final Path uploadPath = Paths.get("uploads");
    
    public ProductMediaController(ProductMediaService mediaService) {
        this.mediaService = mediaService;
        // Create the upload directory if it doesn't exist
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductMediaDTO>> getMediaByProductId(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(mediaService.getAllMediaByProductId(productId));
    }

    @GetMapping("/product/{productId}/all")
    public ResponseEntity<List<ProductMediaDTO>> getAllMediaByProductId(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(mediaService.getAllMediaByProductId(productId));
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<ProductMediaDTO>> getMediaByVariantId(@PathVariable("variantId") Long variantId) {
        return ResponseEntity.ok(mediaService.getMediaByVariantId(variantId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductMediaDTO> getMediaById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }
    
    @PostMapping(value = "/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductMediaDTO> addMedia(
            @PathVariable("productId") Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "variantId", required = false) String variantIdStr,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            @RequestParam(value = "displayOrder", required = false, defaultValue = "0") String displayOrderStr) {
        
        Long variantId = (variantIdStr != null && !variantIdStr.isEmpty() && !variantIdStr.equals("null")) ? Long.parseLong(variantIdStr) : null;
        Integer displayOrder = (displayOrderStr != null && !displayOrderStr.isEmpty()) ? Integer.parseInt(displayOrderStr) : 0;
        
        String fileDownloadUri = saveUploadedFile(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.addMedia(productId, variantId, fileDownloadUri, type, displayOrder));
    }

    @PostMapping(value = "/product/{productId}/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductMediaDTO> addMediaByUrl(
            @PathVariable("productId") Long productId,
            @RequestBody ProductMediaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.addMediaByUrl(
                        productId,
                        request.getVariantId(),
                        request.getUrl(),
                        request.getType(),
                        request.getDisplayOrder()));
    }
    
    @PostMapping(value = "/product/{productId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductMediaDTO> uploadImage(
            @PathVariable("productId") Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "variantId", required = false) String variantIdStr,
            @RequestParam(value = "type", required = false, defaultValue = "IMAGE") String type,
            @RequestParam(value = "displayOrder", required = false, defaultValue = "0") String displayOrderStr) {
        
        Long variantId = (variantIdStr != null && !variantIdStr.isEmpty() && !variantIdStr.equals("null")) ? Long.parseLong(variantIdStr) : null;
        Integer displayOrder = (displayOrderStr != null && !displayOrderStr.isEmpty()) ? Integer.parseInt(displayOrderStr) : 0;
        
        String fileDownloadUri = saveUploadedFile(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.addMedia(productId, variantId, fileDownloadUri, type, displayOrder));
    }
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductMediaDTO> updateMedia(
            @PathVariable("id") Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "displayOrder", required = false) String displayOrderStr) {
        
        Integer displayOrder = (displayOrderStr != null && !displayOrderStr.isEmpty()) ? Integer.parseInt(displayOrderStr) : null;
        String fileDownloadUri = file != null ? saveUploadedFile(file) : null;
        return ResponseEntity.ok(mediaService.updateMedia(id, fileDownloadUri, type, displayOrder));
    }

    @PutMapping(value = "/{id}/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductMediaDTO> updateMediaByUrl(
            @PathVariable("id") Long id,
            @RequestBody ProductMediaUpdateRequest request) {
        return ResponseEntity.ok(
                mediaService.updateMediaByUrl(id, request.getUrl(), request.getType(), request.getDisplayOrder()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable("id") Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }

    private String saveUploadedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select a file to upload");
        }

        try {
            byte[] bytes = file.getBytes();
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String newFileName = UUID.randomUUID() + fileExtension;
            Path path = uploadPath.resolve(Paths.get(newFileName)).normalize().toAbsolutePath();
            Files.write(path, bytes);

            return "/uploads/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not upload the file: " + e.getMessage());
        }
    }
}
