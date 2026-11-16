package com.example.demo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PenjualRepository extends JpaRepository<Penjual, Long> {
    Optional<Penjual> findByEmail(String email);
}
