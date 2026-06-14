package org.example.project_cuoiky_congnghephanmem_oose.service.user.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.example.project_cuoiky_congnghephanmem_oose.repository.ICustomerRepository;
import org.example.project_cuoiky_congnghephanmem_oose.service.user.IAvatarService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class AvatarServiceImpl implements IAvatarService {

    private final Cloudinary cloudinary;
    private final ICustomerRepository customerRepository;

    public AvatarServiceImpl(Cloudinary cloudinary, ICustomerRepository customerRepository) {
        this.cloudinary = cloudinary;
        this.customerRepository = customerRepository;
    }

    @Override
    public String uploadAvatar(MultipartFile file, String username) {
        try {
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            if (customer.isGoogleAccount()) {
                throw new RuntimeException("Tài khoản Google sử dụng ảnh đại diện từ Google, không thể thay đổi");
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "hotel-style/avatar",
                            "public_id", "user_" + customer.getUserID(),
                            "overwrite", true,
                            "invalidate", true,
                            "resource_type", "image"
                    )
            );

            String avatarUrl = uploadResult.get("secure_url").toString();
            customer.setAvatar(avatarUrl);
            customerRepository.save(customer);

            return avatarUrl;
        } catch (Exception e) {
            throw new RuntimeException("Upload avatar thất bại: " + e.getMessage());
        }
    }
}