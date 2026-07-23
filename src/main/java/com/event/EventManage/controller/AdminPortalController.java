package com.event.EventManage.controller;

import com.event.EventManage.dto.ApiResponse;
import com.event.EventManage.model.*;
import com.event.EventManage.repository.*;
import com.event.EventManage.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminPortalController {

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final VendorRepository vendorRepository;
    private final NotificationRepository notificationRepository;
    private final CmsContentRepository cmsContentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final ReviewRepository reviewRepository;

    // ==========================================
    // USER MANAGEMENT
    // ==========================================
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("Admin request: get all users");
        return ResponseEntity.ok(ApiResponse.success(userRepository.findAll(), "Users retrieved successfully", HttpStatus.OK.value()));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUserRole(@PathVariable String id, @RequestParam String role, Authentication auth) {
        log.info("Admin {} changing role of user {} to {}", auth.getName(), id, role);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setRole(Role.valueOf(role.toUpperCase()));
        User updated = userRepository.save(user);

        // Audit Log
        activityLogRepository.save(ActivityLog.builder()
                .action("UPDATE_USER_ROLE")
                .userEmail(auth.getName())
                .details("Updated role of " + user.getEmail() + " to " + role)
                .build());

        return ResponseEntity.ok(ApiResponse.success(updated, "User role updated successfully", HttpStatus.OK.value()));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id, Authentication auth) {
        log.info("Admin {} deleting user {}", auth.getName(), id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmail().equalsIgnoreCase(auth.getName())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("You cannot delete your own account", HttpStatus.BAD_REQUEST.value()));
        }

        userRepository.delete(user);

        // Audit Log
        activityLogRepository.save(ActivityLog.builder()
                .action("DELETE_USER")
                .userEmail(auth.getName())
                .details("Deleted user account: " + user.getEmail())
                .build());

        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully", HttpStatus.OK.value()));
    }

    // ==========================================
    // COUPONS / PROMOTIONS
    // ==========================================
    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<List<Coupon>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success(couponRepository.findAll(), "Coupons retrieved", HttpStatus.OK.value()));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(@RequestBody Coupon coupon, Authentication auth) {
        Coupon saved = couponRepository.save(coupon);
        activityLogRepository.save(ActivityLog.builder()
                .action("CREATE_COUPON")
                .userEmail(auth.getName())
                .details("Created coupon code: " + coupon.getCode())
                .build());
        return new ResponseEntity<>(ApiResponse.success(saved, "Coupon created", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Coupon>> updateCoupon(@PathVariable String id, @RequestBody Coupon couponDetails, Authentication auth) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        coupon.setCode(couponDetails.getCode());
        coupon.setDiscountAmount(couponDetails.getDiscountAmount());
        coupon.setDiscountType(couponDetails.getDiscountType());
        coupon.setExpiryDate(couponDetails.getExpiryDate());
        coupon.setActive(couponDetails.isActive());
        coupon.setUsageLimit(couponDetails.getUsageLimit());
        
        Coupon updated = couponRepository.save(coupon);
        activityLogRepository.save(ActivityLog.builder()
                .action("UPDATE_COUPON")
                .userEmail(auth.getName())
                .details("Updated coupon code: " + coupon.getCode())
                .build());
        return ResponseEntity.ok(ApiResponse.success(updated, "Coupon updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable String id, Authentication auth) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        couponRepository.delete(coupon);
        activityLogRepository.save(ActivityLog.builder()
                .action("DELETE_COUPON")
                .userEmail(auth.getName())
                .details("Deleted coupon code: " + coupon.getCode())
                .build());
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon deleted", HttpStatus.OK.value()));
    }

    // ==========================================
    // VENDORS / PARTNERS
    // ==========================================
    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse<List<Vendor>>> getAllVendors() {
        return ResponseEntity.ok(ApiResponse.success(vendorRepository.findAll(), "Vendors retrieved", HttpStatus.OK.value()));
    }

    @PostMapping("/vendors")
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody Vendor vendor, Authentication auth) {
        Vendor saved = vendorRepository.save(vendor);
        activityLogRepository.save(ActivityLog.builder()
                .action("CREATE_VENDOR")
                .userEmail(auth.getName())
                .details("Created vendor: " + vendor.getName())
                .build());
        return new ResponseEntity<>(ApiResponse.success(saved, "Vendor created", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(@PathVariable String id, @RequestBody Vendor vendorDetails, Authentication auth) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setName(vendorDetails.getName());
        vendor.setServiceType(vendorDetails.getServiceType());
        vendor.setEmail(vendorDetails.getEmail());
        vendor.setPhone(vendorDetails.getPhone());
        vendor.setRating(vendorDetails.getRating());
        vendor.setActive(vendorDetails.isActive());

        Vendor updated = vendorRepository.save(vendor);
        activityLogRepository.save(ActivityLog.builder()
                .action("UPDATE_VENDOR")
                .userEmail(auth.getName())
                .details("Updated vendor: " + vendor.getName())
                .build());
        return ResponseEntity.ok(ApiResponse.success(updated, "Vendor updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable String id, Authentication auth) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendorRepository.delete(vendor);
        activityLogRepository.save(ActivityLog.builder()
                .action("DELETE_VENDOR")
                .userEmail(auth.getName())
                .details("Deleted vendor: " + vendor.getName())
                .build());
        return ResponseEntity.ok(ApiResponse.success(null, "Vendor deleted", HttpStatus.OK.value()));
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getAllNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationRepository.findByOrderByCreatedAtDesc(), "Notifications retrieved", HttpStatus.OK.value()));
    }

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<Notification>> createNotification(@RequestBody Notification notification, Authentication auth) {
        Notification saved = notificationRepository.save(notification);
        return new ResponseEntity<>(ApiResponse.success(saved, "Notification created", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    @PostMapping("/notifications/mark-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        List<Notification> list = notificationRepository.findAll();
        for (Notification n : list) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(list);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read", HttpStatus.OK.value()));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted", HttpStatus.OK.value()));
    }

    // ==========================================
    // CMS CONTENT
    // ==========================================
    @GetMapping("/cms")
    public ResponseEntity<ApiResponse<List<CmsContent>>> getAllCms() {
        return ResponseEntity.ok(ApiResponse.success(cmsContentRepository.findAll(), "CMS content retrieved", HttpStatus.OK.value()));
    }

    @PostMapping("/cms")
    public ResponseEntity<ApiResponse<CmsContent>> saveCms(@RequestBody CmsContent cmsDetails, Authentication auth) {
        Optional<CmsContent> existing = cmsContentRepository.findByContentKey(cmsDetails.getContentKey());
        CmsContent saved;
        if (existing.isPresent()) {
            CmsContent cms = existing.get();
            cms.setTitle(cmsDetails.getTitle());
            cms.setContentHtml(cmsDetails.getContentHtml());
            cms.setCategory(cmsDetails.getCategory());
            saved = cmsContentRepository.save(cms);
        } else {
            saved = cmsContentRepository.save(cmsDetails);
        }
        activityLogRepository.save(ActivityLog.builder()
                .action("SAVE_CMS_CONTENT")
                .userEmail(auth.getName())
                .details("Saved CMS item: " + saved.getContentKey())
                .build());
        return ResponseEntity.ok(ApiResponse.success(saved, "CMS content saved", HttpStatus.OK.value()));
    }

    @GetMapping("/cms/{key}")
    public ResponseEntity<ApiResponse<CmsContent>> getCmsByKey(@PathVariable String key) {
        CmsContent content = cmsContentRepository.findByContentKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("CMS content not found with key: " + key));
        return ResponseEntity.ok(ApiResponse.success(content, "CMS content retrieved", HttpStatus.OK.value()));
    }

    // ==========================================
    // ACTIVITY / AUDIT LOGS
    // ==========================================
    @GetMapping("/activity-logs")
    public ResponseEntity<ApiResponse<List<ActivityLog>>> getAllLogs() {
        return ResponseEntity.ok(ApiResponse.success(activityLogRepository.findByOrderByTimestampDesc(), "Activity logs retrieved", HttpStatus.OK.value()));
    }

    @PostMapping("/activity-logs")
    public ResponseEntity<ApiResponse<ActivityLog>> createLog(@RequestBody ActivityLog logEntry) {
        ActivityLog saved = activityLogRepository.save(logEntry);
        return new ResponseEntity<>(ApiResponse.success(saved, "Log saved", HttpStatus.CREATED.value()), HttpStatus.CREATED);
    }

    // ==========================================
    // SYSTEM SETTINGS
    // ==========================================
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<SystemSetting>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(systemSettingRepository.findAll(), "System settings retrieved", HttpStatus.OK.value()));
    }

    @PostMapping("/settings")
    public ResponseEntity<ApiResponse<SystemSetting>> saveSetting(@RequestBody SystemSetting settingDetails, Authentication auth) {
        Optional<SystemSetting> existing = systemSettingRepository.findBySettingKey(settingDetails.getSettingKey());
        SystemSetting saved;
        if (existing.isPresent()) {
            SystemSetting setting = existing.get();
            setting.setSettingValue(settingDetails.getSettingValue());
            setting.setDescription(settingDetails.getDescription());
            saved = systemSettingRepository.save(setting);
        } else {
            saved = systemSettingRepository.save(settingDetails);
        }
        activityLogRepository.save(ActivityLog.builder()
                .action("SAVE_SYSTEM_SETTING")
                .userEmail(auth.getName())
                .details("Saved setting: " + saved.getSettingKey() + " = " + saved.getSettingValue())
                .build());
        return ResponseEntity.ok(ApiResponse.success(saved, "Setting saved", HttpStatus.OK.value()));
    }

    // ==========================================
    // REVIEWS MODERATION
    // ==========================================
    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviews() {
        return ResponseEntity.ok(ApiResponse.success(reviewRepository.findAll(), "Reviews retrieved successfully", HttpStatus.OK.value()));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable String id, Authentication auth) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        reviewRepository.delete(review);
        activityLogRepository.save(ActivityLog.builder()
                .action("DELETE_REVIEW")
                .userEmail(auth.getName())
                .details("Deleted review by " + review.getUser().getEmail() + " on item " + review.getItem().getName())
                .build());
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully", HttpStatus.OK.value()));
    }
}
