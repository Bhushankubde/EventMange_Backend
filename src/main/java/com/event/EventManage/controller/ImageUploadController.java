package com.event.EventManage.controller;

import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.model.Item;
import com.event.EventManage.repository.ItemRepository;
import com.event.EventManage.service.ImageUploadService;
import com.event.EventManage.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;
    private final ItemRepository itemRepository;

    /**
     * Upload an image for a specific item.
     * Admin only.
     * POST /api/uploads/items/{itemId}
     */
    @PostMapping("/items/{itemId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadItemImage(
            @PathVariable String itemId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        log.info("Received image upload request for item ID: {}", itemId);

        // Verify item exists
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        // Build base URL dynamically
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        // Upload file and get public URL
        String imageUrl = imageUploadService.uploadItemImage(itemId, file, baseUrl);

        // Update item's imageUrl in database
        item.setImageUrl(imageUrl);
        itemRepository.save(item);

        log.info("Image uploaded and item updated. URL: {}", imageUrl);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("imageUrl", imageUrl),
                "Image uploaded successfully",
                HttpStatus.OK.value()
        ));
    }

    /**
     * Serve uploaded images.
     * Public endpoint.
     * GET /api/uploads/{itemId}/{filename}
     */
    @GetMapping("/{itemId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String itemId,
            @PathVariable String filename,
            HttpServletRequest request) {

        log.info("Serving uploaded image: items/{}/{}", itemId, filename);

        Path filePath = imageUploadService.resolveFilePath(itemId, filename);

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Image not found or unreadable: " + filename);
            }

            // Detect content type dynamically
            String contentType;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (Exception e) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }
            if (contentType == null) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Image not found: " + filename);
        }
    }
}
