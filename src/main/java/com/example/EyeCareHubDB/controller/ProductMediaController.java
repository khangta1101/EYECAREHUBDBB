package com.example.EyeCareHubDB.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    public ResponseEntity<List<ProductMediaDTO>> getMediaByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(mediaService.getMediaByProductId(productId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductMediaDTO> getMediaById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }
    
    @GetMapping("/product/{productId}/primary")
    public ResponseEntity<ProductMediaDTO> getPrimaryMedia(@PathVariable Long productId) {
        return ResponseEntity.ok(mediaService.getPrimaryMedia(productId));
    }
    
    @GetMapping("/product/{productId}/images")
    public ResponseEntity<List<ProductMediaDTO>> getImagesByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(mediaService.getImagesByProductId(productId));
    }
    
    @PostMapping("/product/{productId}")
    public ResponseEntity<ProductMediaDTO> addMedia(
            @PathVariable Long productId,
            @RequestBody ProductMediaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.addMedia(productId, request));
    }
    
    @PostMapping("/product/{productId}/upload")
    public ResponseEntity<ProductMediaDTO> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "displayOrder", required = false, defaultValue = "0") Integer displayOrder,
            @RequestParam(value = "isPrimary", required = false, defaultValue = "false") Boolean isPrimary) {
        
        if (file.isEmpty()) {
            throw new RuntimeException("Please select a file to upload");
        }
        
        try {
            // Save the file
            byte[] bytes = file.getBytes();
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            // Generate a unique file name
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            Path path = uploadPath.resolve(Paths.get(newFileName)).normalize().toAbsolutePath();
            Files.write(path, bytes);
            
            // Construct the file download URI
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(newFileName)
                    .toUriString();
            
            // Create the media with the uploaded file URL
            ProductMediaCreateRequest request = ProductMediaCreateRequest.builder()
                    .type("IMAGE")
                    .url(fileDownloadUri)
                    .altText(altText)
                    .title(title)
                    .displayOrder(displayOrder)
                    .isPrimary(isPrimary)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mediaService.addMedia(productId, request));
                    
        } catch (IOException e) {
            throw new RuntimeException("Could not upload the file: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductMediaDTO> updateMedia(
            @PathVariable Long id,
            @RequestBody ProductMediaUpdateRequest request) {
        return ResponseEntity.ok(mediaService.updateMedia(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}
