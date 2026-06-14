package org.example.project_cuoiky_congnghephanmem_oose.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    // ID token (JWT) do Google Identity Services trả về ở frontend
    @NotBlank(message = "Thiếu credential từ Google")
    private String credential;
}
