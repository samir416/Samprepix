package com.aiinterview.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public FileStorageService(Path uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String saveProfilePicture(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                (!contentType.equals("image/png")
                        && !contentType.equals("image/jpeg")
                        && !contentType.equals("image/jpg")
                        && !contentType.equals("image/webp"))) {

            throw new IllegalArgumentException("Only PNG, JPG, JPEG and WEBP are allowed.");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        String fileName = UUID.randomUUID() + "." + extension;

        Path target = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), target);

        return "/" + uploadDir + "/" + fileName;
    }

   public void deleteProfilePicture(String imagePath) throws IOException {

    if (imagePath == null || imagePath.isBlank()) {
        return;
    }

    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        return;
    }

    String fileName = Path.of(imagePath).getFileName().toString();

    Files.deleteIfExists(uploadPath.resolve(fileName));
}

}