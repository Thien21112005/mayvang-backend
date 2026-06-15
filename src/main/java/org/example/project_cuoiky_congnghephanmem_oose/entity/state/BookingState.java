package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

/**
 * State Pattern – biểu diễn trạng thái vòng đời của một Booking dưới dạng đối tượng.
 *
 * <p>Mỗi trạng thái cụ thể ({@link PendingState}, {@link ConfirmedState}, ...) tự
 * quyết định:
 * <ul>
 *   <li>Các <b>guard</b> (truy vấn) hợp lệ ở trạng thái đó: có cho thanh toán/hủy không,
 *       có được tính doanh thu không, có phải trạng thái kết thúc không.</li>
 *   <li>Các <b>transition</b> (chuyển trạng thái) hợp lệ: trả về {@code BookingState}
 *       kế tiếp; transition không hợp lệ sẽ ném {@link IllegalStateException}.</li>
 * </ul>
 *
 * <p>Các trạng thái là singleton, không lưu state nội tại (stateless) nên dùng chung
 * an toàn. Chuỗi {@link #getStatusName()} trùng khít với chuỗi đang lưu trong DB.
 */
public interface BookingState {

    /** Chuỗi trạng thái lưu xuống DB (vd: "pending"). */
    String getStatusName();

    /** Nhãn hiển thị tiếng Việt cho trạng thái (phục vụ thông báo lỗi/UI). */
    String getDisplayName();

    // ===== Guards (truy vấn, không làm thay đổi trạng thái) =====

    /** Đang ở trạng thái chờ thanh toán. */
    default boolean isPending() {
        return false;
    }

    /** Có thể tiến hành thanh toán (chỉ khi đang chờ thanh toán). */
    default boolean canBePaid() {
        return false;
    }

    /** Khách hàng có thể tự hủy (chỉ khi đang chờ thanh toán). */
    default boolean canBeCancelled() {
        return false;
    }

    /** Được tính là đã thanh toán (tính vào doanh thu). */
    default boolean isPaid() {
        return false;
    }

    /** Trạng thái kết thúc, không chuyển tiếp được nữa. */
    default boolean isFinal() {
        return false;
    }

    // ===== Transitions (trả về trạng thái kế tiếp; mặc định: không hợp lệ) =====

    default BookingState confirmPayment() {
        throw new IllegalStateException("Không thể xác nhận thanh toán từ trạng thái: " + getDisplayName());
    }

    default BookingState cancel() {
        throw new IllegalStateException("Không thể hủy từ trạng thái: " + getDisplayName());
    }

    default BookingState checkIn() {
        throw new IllegalStateException("Không thể nhận phòng từ trạng thái: " + getDisplayName());
    }

    default BookingState checkOut() {
        throw new IllegalStateException("Không thể trả phòng từ trạng thái: " + getDisplayName());
    }
}
