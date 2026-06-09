package org.example.project_cuoiky_congnghephanmem_oose.service.auth;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.LoginRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.RegisterRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.response.AuthResponse;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.RegisterRequest;
import org.example.project_cuoiky_congnghephanmem_oose.dto.request.LoginRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void forgotPassword(String email);
    boolean verifyOtp(String email, String otpCode);
    void resetPassword(String email, String newPassword);
}