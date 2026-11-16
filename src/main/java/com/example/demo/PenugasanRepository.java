package com.example.demo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PenugasanRepository extends JpaRepository<Penugasan, Long> {
    Optional<Penugasan> findByPenjualanId(Long penjualanId);
}
