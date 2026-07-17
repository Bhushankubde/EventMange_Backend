package com.event.EventManage.service;

import com.event.EventManage.exception.BadRequestException;
import com.event.EventManage.exception.ResourceNotFoundException;
import com.event.EventManage.model.Item;
import com.event.EventManage.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * Uploads an image file for a specific item and returns the accessible URL path.
     *
     * @param itemId    the ID of the item
     * @param file      the uploaded multipart file
     * @param baseUrl   the base server URL (e.g. http://localhost:8080)
     * @return the public URL path to access the uploaded file
     */
    public String uploadItemImage(String itemId, MultipartFile file, String baseUrl) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + fileExtension;

        try {
            // Create directory: uploads/images/{itemId}/
            Path itemUploadPath = Paths.get(uploadDir, itemId).toAbsolutePath().normalize();
            Files.createDirectories(itemUploadPath);

            // Copy the uploaded file to destination
            Path targetPath = itemUploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Uploaded image for item {} -> {}", itemId, targetPath);

            // Return the accessible URL
            return baseUrl + "/api/uploads/" + itemId + "/" + storedFilename;

        } catch (IOException e) {
            log.error("Failed to store image for item {}: {}", itemId, e.getMessage());
            throw new RuntimeException("Failed to store image. Please try again.", e);
        }
    }

    /**
     * Resolves the file path on disk for serving a stored image.
     *
     * @param itemId   the item ID (subdirectory)
     * @param filename the stored filename
     * @return resolved Path
     */
    public Path resolveFilePath(String itemId, String filename) {
        try {
            Path filePath = Paths.get(uploadDir, itemId)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(filename)
                    .normalize();

            if (!Files.exists(filePath)) {
                throw new ResourceNotFoundException("Image not found: " + filename);
            }
            return filePath;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Image not found: " + filename);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, GIF and WebP images are allowed.");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File size must not exceed 10 MB.");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
