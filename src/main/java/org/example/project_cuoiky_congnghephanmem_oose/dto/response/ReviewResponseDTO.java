package org.example.project_cuoiky_congnghephanmem_oose.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private int reviewID;
    private double rating;
    private String comment;
    private LocalDateTime reviewDate;
    private String adminReply;
    private LocalDateTime replyDate;
    private String customerName;
    private String customerAvatar;
    private String roomType;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private int bookingID;

    private Integer cleanlinessRating;
    private Integer serviceRating;
    private Integer facilitiesRating;
    private Integer locationRating;
    private String imageUrls;
    private boolean isAnonymous;
}
