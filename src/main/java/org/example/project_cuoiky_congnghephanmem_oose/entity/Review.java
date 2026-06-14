package org.example.project_cuoiky_congnghephanmem_oose.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reviewID;

    private double rating; // 1 to 5, có thể lẻ (vd: 4.75)

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime reviewDate;

    private Integer cleanlinessRating;
    private Integer serviceRating;
    private Integer facilitiesRating;
    private Integer locationRating;

    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isAnonymous;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private LocalDateTime replyDate;

    @ManyToOne
    @JoinColumn(name = "customerID")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "bookingID")
    private Booking booking;

}
