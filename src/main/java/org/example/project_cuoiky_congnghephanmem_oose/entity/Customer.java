package org.example.project_cuoiky_congnghephanmem_oose.entity;

import jakarta.persistence.*;
import lombok.*;

import org.example.project_cuoiky_congnghephanmem_oose.enums.Role;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Customer")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "userID")
public class Customer extends User {

    private int point;

    @ManyToOne
    @JoinColumn(name = "tierID")
    private MembershipTier membershipTier;

    public Customer() {
        super();
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }

    public Booking makeBooking() {
        return null;
    }

    public List<Rooms> searchRoom(LocalDate checkin, LocalDate checkout, String type, int occupancy) {
        return null;
    }

    public List<Booking> viewBookingHistory() {
        return null;
    }

    public MembershipTier trackMembership() {
        return null;
    }
}