package com.example.demo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PetugasRepository extends JpaRepository<Petugas, Long> {
    Optional<Petugas> findByEmail(String email);
    long countByStatusIgnoreCase(String status);
    List<Petugas> findByStatusIgnoreCase(String status);
}
