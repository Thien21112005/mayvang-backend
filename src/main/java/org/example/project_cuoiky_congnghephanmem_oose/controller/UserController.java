package org.example.project_cuoiky_congnghephanmem_oose.controller;

import jakarta.validation.Valid;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.UpdateProfileRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.AvatarUploadResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.BookingHistoryResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.UserProfileResponse;
import org.example.project_cuoiky_congnghephanmem_oose.service.user.IAvatarService;
import org.example.project_cuoiky_congnghephanmem_oose.service.user.IUserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;
    private final IAvatarService avatarService;

    public UserController(IUserService userService, IAvatarService avatarService) {
        this.userService = userService;
        this.avatarService = avatarService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getMyProfile(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateMyProfile(authentication.getName(), request));
    }

    @GetMapping("/me/bookings")
    public ResponseEntity<List<BookingHistoryResponse>> getMyBookings(Authentication authentication) {
        return ResponseEntity.ok(userService.getMyBookingHistory(authentication.getName()));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarUploadResponse> uploadAvatar(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {
        String avatarUrl = avatarService.uploadAvatar(file, authentication.getName());
        return ResponseEntity.ok(new AvatarUploadResponse(avatarUrl, "Upload avatar thành công"));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody org.example.project_cuoiky_congnghephanmem_oose.dto.request.ChangePasswordRequest request
    ) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PutMapping("/me/bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            Authentication authentication,
            @PathVariable Integer bookingId
    ) {
        userService.cancelBooking(authentication.getName(), bookingId);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Đã hủy đơn đặt phòng"));
    }
}