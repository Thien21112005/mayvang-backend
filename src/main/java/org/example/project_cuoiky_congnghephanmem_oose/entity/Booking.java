package org.example.project_cuoiky_congnghephanmem_oose.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.time.LocalDateTime;

import org.example.project_cuoiky_congnghephanmem_oose.entity.state.BookingState;
import org.example.project_cuoiky_congnghephanmem_oose.entity.state.BookingStateFactory;

@Entity
@Table(name = "Booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingID;
    private LocalDateTime bookingDate;
    private double totalPrice;
    private String status;
    private LocalDateTime expiredAt;

    @ManyToOne
    @JoinColumn(name = "userID")
    private Customer customer;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingDetails> bookingDetails;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Payment> payments;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Review review;

    public Booking createBooking() {
        return null;
    }

    public void cancelBooking() {
    }

    public boolean checkExpired() {
        return false;
    }

    // ===================== State Pattern =====================
    // Trạng thái booking (chuỗi `status`) được "diễn giải" thành đối tượng BookingState.
    // Field `status` vẫn là nguồn lưu trữ duy nhất xuống DB; các method dưới đây chỉ
    // ủy quyền (delegate) cho state hiện tại nên hành vi không thay đổi.

    /** Đối tượng trạng thái tương ứng với chuỗi `status` hiện tại. */
    @Transient
    public BookingState getBookingState() {
        return BookingStateFactory.from(this.status);
    }

    /** Đang chờ thanh toán. */
    @Transient
    public boolean isPending() {
        return getBookingState().isPending();
    }

    /** Có thể tiến hành thanh toán (chỉ khi đang chờ thanh toán). */
    @Transient
    public boolean canBePaid() {
        return getBookingState().canBePaid();
    }

    /** Khách hàng có thể tự hủy (chỉ khi đang chờ thanh toán). */
    @Transient
    public boolean canBeCancelled() {
        return getBookingState().canBeCancelled();
    }

    /** Được tính là đã thanh toán (tính vào doanh thu). */
    @Transient
    public boolean isPaid() {
        return getBookingState().isPaid();
    }

    // ---- Transition: chuyển trạng thái qua State, ghi lại chuỗi tương ứng ----

    /** pending → confirmed (sau khi thanh toán thành công). */
    public void confirmPayment() {
        this.status = getBookingState().confirmPayment().getStatusName();
    }

    /** pending → cancelled (hết hạn giữ phòng / khách hủy / thanh toán thất bại đã hết hạn). */
    public void cancel() {
        this.status = getBookingState().cancel().getStatusName();
    }

    /** confirmed → checked_in (đến ngày nhận phòng). */
    public void checkIn() {
        this.status = getBookingState().checkIn().getStatusName();
    }

    /** confirmed/checked_in → checked_out (qua ngày trả phòng). */
    public void checkOut() {
        this.status = getBookingState().checkOut().getStatusName();
    }
}