package com.example.EyeCareHubDB.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final Path uploadPath = Paths.get("uploads");

    public FileService() {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public String saveFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select a file to upload");
        }

        try {
            Path targetDir = subDir != null ? uploadPath.resolve(subDir) : uploadPath;
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            byte[] bytes = file.getBytes();
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String newFileName = UUID.randomUUID() + fileExtension;
            Path path = targetDir.resolve(newFileName).normalize().toAbsolutePath();
            Files.write(path, bytes);

            String relativePath = "/uploads/" + (subDir != null ? subDir + "/" : "") + newFileName;
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Could not upload the file: " + e.getMessage());
        }
    }
}
