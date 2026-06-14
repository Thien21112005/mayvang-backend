package org.example.project_cuoiky_congnghephanmem_oose.service.manager;

import org.example.project_cuoiky_congnghephanmem_oose.dto.request.CreateManagerRequest;

public interface IManagerAccountService {
    // Tạo 1 tài khoản quản lý mới, trả về username vừa tạo
    String createManager(CreateManagerRequest request);
}
