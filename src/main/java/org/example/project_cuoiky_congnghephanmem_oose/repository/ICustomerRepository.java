package org.example.project_cuoiky_congnghephanmem_oose.repository;

import org.example.project_cuoiky_congnghephanmem_oose.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPhone(String phone);
}