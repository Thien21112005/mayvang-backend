package org.example.project_cuoiky_congnghephanmem_oose.entity;

import jakarta.persistence.*;
import lombok.*;

import org.example.project_cuoiky_congnghephanmem_oose.enums.Role;

import java.util.List;

@Entity
@Table(name = "Manager")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "userID")
public class Manager extends User {

    public Manager() {
        super();
    }

    @Override
    public Role getRole() {
        return Role.MANAGER;
    }

    public List<Booking> viewAllBookings() {
        return null;
    }

    public void updateRoomStatus(int roomID, String status) {
    }

    public void updateRoomInfo(int roomID) {
    }

    public double viewRevenue() {
        return 0;
    }

    public List<Customer> viewPotentialCustomers() {
        return null;
    }
}