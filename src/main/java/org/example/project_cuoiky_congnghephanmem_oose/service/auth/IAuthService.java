package org.example.project_cuoiky_congnghephanmem_oose.service.auth;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.LoginRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.RegisterRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.AuthResponse;

public interface IAuthService {
    // Bước 1: kiểm tra dữ liệu + gửi OTP về email (CHƯA tạo tài khoản)
    AuthResponse register(RegisterRequest request);
    // Bước 2: xác thực OTP -> tạo tài khoản
    AuthResponse verifyRegister(String email, String otpCode);

    AuthResponse login(LoginRequest request);
    AuthResponse loginWithGoogle(String credential);

    void forgotPassword(String email);
    boolean verifyOtp(String email, String otpCode);
    void resetPassword(String email, String newPassword);
}
