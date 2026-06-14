package org.example.project_cuoiky_congnghephanmem_oose.controller;

import jakarta.validation.Valid;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.ForgotPasswordRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.GoogleLoginRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.LoginRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.RegisterRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.VerifyOtpRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.AuthResponse;
import org.example.project_cuoiky_congnghephanmem_oose.service.auth.IAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // Xác thực OTP đăng ký -> tạo tài khoản
    @PostMapping("/register/verify")
    public ResponseEntity<AuthResponse> verifyRegister(@RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyRegister(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Đăng nhập bằng Google (frontend gửi ID token)
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request.getCredential());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Mã xác thực đã được gửi vào email của bạn!");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean isValid = authService.verifyOtp(request.getEmail(), request.getOtpCode());
        if (isValid) {
            return ResponseEntity.ok("Xác thực OTP thành công!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mã OTP không chính xác hoặc đã hết hạn!");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody org.example.project_cuoiky_congnghephanmem_oose.dto.request.ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok("Đặt lại mật khẩu thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}