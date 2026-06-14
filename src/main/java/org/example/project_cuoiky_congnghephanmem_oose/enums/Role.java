package org.example.project_cuoiky_congnghephanmem_oose.enums;

/**
 * Vai trò người dùng trong hệ thống.
 * Vai trò được suy ra trực tiếp từ kiểu thực thể (Customer / Manager) qua phương thức
 * đa hình User#getRole(), nên không còn lưu "roleID" rời rạc -> chỉ 1 nguồn sự thật.
 */
public enum Role {
    CUSTOMER,
    MANAGER
}
