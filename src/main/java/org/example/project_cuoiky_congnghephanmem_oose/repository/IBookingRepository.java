package org.example.project_cuoiky_congnghephanmem_oose.repository;

import org.example.project_cuoiky_congnghephanmem_oose.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IBookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByCustomerUserIDOrderByBookingDateDesc(int userId);

    List<Booking> findByStatusIgnoreCaseAndExpiredAtBefore(String status, LocalDateTime expiredAt);

    List<Booking> findByBookingDateBetweenAndStatus(
            LocalDateTime from, LocalDateTime to, String status
    );
    List<Booking> findByStatus(String status);

    // Dùng cho doanh thu: tính mọi booking đã thanh toán (confirmed/checked_in/checked_out)
    List<Booking> findByStatusIn(List<String> statuses);
    List<Booking> findByBookingDateBetweenAndStatusIn(
            LocalDateTime from, LocalDateTime to, List<String> statuses
    );

    // Khách hàng tiềm năng (RFM): gom theo khách, tính booking đã thanh toán
    @Query("""
    SELECT b.customer, COUNT(b), SUM(b.totalPrice), MAX(b.bookingDate)
    FROM Booking b
    WHERE b.status IN ('confirmed', 'checked_in', 'checked_out')
    GROUP BY b.customer
    ORDER BY SUM(b.totalPrice) DESC
""")
    List<Object[]> findPotentialCustomers();

    @Query("""
        SELECT DISTINCT b FROM Booking b
        LEFT JOIN FETCH b.bookingDetails bd
        LEFT JOIN FETCH bd.room
        LEFT JOIN FETCH b.customer c
        WHERE (:status IS NULL OR b.status = :status)
        AND (
            :keyword IS NULL
            OR LOWER(c.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        ORDER BY b.bookingDate DESC
    """)
    List<Booking> findBookingsWithFilter(
            @Param("status") String status,
            @Param("keyword") String keyword
    );

    @Query("SELECT bd.room.roomID FROM BookingDetails bd " +
            "WHERE bd.booking.status IN ('confirmed', 'checked_in', 'checked_out') " +
            "AND bd.checkinDate <= :today " +
            "AND bd.checkoutDate > :today")
    List<Integer> findBookedRoomIdsByDate(@Param("today") LocalDate today);
}