// dto/response/PotentialCustomerResponse.java
package org.example.project_cuoiky_congnghephanmem_oose.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PotentialCustomerResponse {
    private int userID;
    private String username;
    private String email;
    private String phone;
    private long totalBookings;
    private double totalSpent;

    // RFM fields
    private int recencyScore;   // 1-3
    private int frequencyScore; // 1-3
    private int monetaryScore;  // 1-3
    private int rfmScore;       // 3-9
    private String rfmLabel;    // VIP, Tiềm năng cao, Cần kích hoạt, Ngủ đông
    private String lastBookingDate;
}