package com.example.EyeCareHubDB.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.EyeCareHubDB.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Generic File Upload APIs")
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestPart("file") MultipartFile file,
                                                     @RequestParam(value = "subDir", defaultValue = "general") String subDir) {
        String url = fileService.saveFile(file, subDir);
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        return ResponseEntity.ok(response);
    }
}
