package com.event.EventManage.controller;

import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.dto.UpdateProfileRequest;
import com.event.EventManage.dto.UserProfileResponse;
import com.event.EventManage.model.User;
import com.event.EventManage.repository.UserRepository;
import com.event.EventManage.service.ImageUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", HttpStatus.UNAUTHORIZED.value()));
        }

        String email = authentication.getName();
        log.info("Fetching profile details for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileResponse profile = mapToProfileResponse(user);
        return ResponseEntity.ok(ApiResponse.success(profile, "User profile retrieved successfully", HttpStatus.OK.value()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUser(
            @RequestBody UpdateProfileRequest request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", HttpStatus.UNAUTHORIZED.value()));
        }

        String email = authentication.getName();
        log.info("Updating profile details for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            user.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage().trim().isEmpty() ? null : request.getProfileImage());
        }

        User updatedUser = userRepository.save(user);
        UserProfileResponse profile = mapToProfileResponse(updatedUser);

        return ResponseEntity.ok(ApiResponse.success(profile, "User profile updated successfully", HttpStatus.OK.value()));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", HttpStatus.UNAUTHORIZED.value()));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        // Reuse ImageUploadService by routing user's avatar to an isolated subdirectory
        String imageUrl = imageUploadService.uploadItemImage("user-" + user.getId(), file, baseUrl);

        user.setProfileImage(imageUrl);
        User updatedUser = userRepository.save(user);
        UserProfileResponse profile = mapToProfileResponse(updatedUser);

        return ResponseEntity.ok(ApiResponse.success(profile, "Profile image uploaded successfully", HttpStatus.OK.value()));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .displayName(user.getFirstName() + " " + user.getLastName())
                .username(user.getEmail())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .build();
    }
}
