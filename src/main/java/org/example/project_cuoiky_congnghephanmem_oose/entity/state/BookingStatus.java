package org.example.project_cuoiky_congnghephanmem_oose.entity.state;

import java.util.List;

/**
 * Tập trung các hằng số tên trạng thái (status) của Booking — đúng bằng chuỗi
 * được lưu xuống cột {@code status} trong DB (đều viết thường).
 *
 * <p>Mục tiêu: loại bỏ "magic string" rải rác trong service/scheduler, đồng thời
 * giữ giá trị chuỗi y hệt như code cũ để KHÔNG thay đổi dữ liệu/hành vi.
 */
public final class BookingStatus {

    public static final String PENDING = "pending";
    public static final String CONFIRMED = "confirmed";
    public static final String CHECKED_IN = "checked_in";
    public static final String CHECKED_OUT = "checked_out";
    public static final String CANCELLED = "cancelled";

    /** Các trạng thái được tính là "đã thanh toán" (dùng cho doanh thu, RFM...). */
    public static final List<String> PAID = List.of(CONFIRMED, CHECKED_IN, CHECKED_OUT);

    private BookingStatus() {
    }
}
