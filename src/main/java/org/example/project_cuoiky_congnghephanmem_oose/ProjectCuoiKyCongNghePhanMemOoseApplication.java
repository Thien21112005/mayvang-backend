package org.example.project_cuoiky_congnghephanmem_oose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ProjectCuoiKyCongNghePhanMemOoseApplication {

    @PostConstruct
    public void init() {
        // Ép toàn bộ Server (dù là Render hay Local) đều chạy chung múi giờ Việt Nam
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ProjectCuoiKyCongNghePhanMemOoseApplication.class, args);
    }
}