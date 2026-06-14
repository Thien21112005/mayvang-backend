package org.example.project_cuoiky_congnghephanmem_oose.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import org.example.project_cuoiky_congnghephanmem_oose.enums.Role;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userID;
    private String username;
    private String email;
    private String password;
    private String phone;
    private LocalDate dateOfBirth;
    private String avatar;

    // Nguồn đăng nhập: "LOCAL" (đăng ký thường) hoặc "GOOGLE" (đăng nhập bằng Google)
    @Column(columnDefinition = "varchar(20) default 'LOCAL'")
    private String authProvider;

    /**
     * Vai trò được quyết định bởi chính kiểu thực thể (Customer -> CUSTOMER, Manager -> MANAGER).
     * Đây là nguồn sự thật DUY NHẤT cho phân quyền — không thể lệch như khi lưu roleID thủ công.
     */
    public abstract Role getRole();

    // Tài khoản Google: không cho đổi avatar / mật khẩu / username / email
    public boolean isGoogleAccount() {
        return "GOOGLE".equalsIgnoreCase(authProvider);
    }
}
