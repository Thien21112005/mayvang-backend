package org.example.project_cuoiky_congnghephanmem_oose.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private int userID;
    private String username;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String avatar;
    private int point;
    private String membershipTier;
    private double discountRate;
    private String benefits;

    // true nếu tài khoản đăng nhập bằng Google (khóa đổi avatar/mật khẩu/username/email)
    private boolean googleAccount;

    // JWT mới (chỉ trả khi username thay đổi, để frontend cập nhật token)
    private String token;
}