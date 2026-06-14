package org.example.project_cuoiky_congnghephanmem_oose.service.manager.impl;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.CreateManagerRequest;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Manager;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IManagerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.repository.IUserRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.manager.IManagerAccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ManagerAccountServiceImpl implements IManagerAccountService {

    private final IManagerRepository managerRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerAccountServiceImpl(IManagerRepository managerRepository,
                                     IUserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        this.managerRepository = managerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String createManager(CreateManagerRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        Manager manager = new Manager();
        manager.setUsername(username);
        manager.setEmail(email);
        manager.setPassword(passwordEncoder.encode(request.getPassword()));
        manager.setAuthProvider("LOCAL");
        managerRepository.save(manager);

        return username;
    }
}
