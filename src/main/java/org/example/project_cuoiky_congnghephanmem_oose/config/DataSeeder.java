package org.example.project_cuoiky_congnghephanmem_oose.config;

import org.example.project_cuoiky_congnghephanmem_oose.entity.Manager;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IManagerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tạo sẵn tài khoản quản lý mặc định khi khởi động (nếu hệ thống chưa có quản lý nào).
 * Nhờ vậy không phải insert tay vào DB nữa.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final IManagerRepository managerRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.email:admin@mayvang.com}")
    private String adminEmail;

    public DataSeeder(IManagerRepository managerRepository,
                      IUserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Đã có quản lý hoặc username đã bị dùng thì bỏ qua
        if (managerRepository.count() > 0 || userRepository.existsByUsername(adminUsername)) {
            return;
        }

        Manager manager = new Manager();
        manager.setUsername(adminUsername);
        manager.setEmail(adminEmail);
        manager.setPassword(passwordEncoder.encode(adminPassword));
        manager.setAuthProvider("LOCAL");
        managerRepository.save(manager);

        System.out.println("[DATA_SEEDER] Da tao tai khoan quan ly mac dinh: username=" + adminUsername);
    }
}
