package com.example.demo.repository;

// =========================================================================
// KONSEP OOP PADA INTERFACE INI:
// 1. INHERITANCE (Pewarisan Interface): Mewarisi seluruh operasi CRUD standar
//    (save, findById, findAll, delete) dari JpaRepository<Penjual, Long>.
// 2. POLYMORPHISM (Polimorfisme): Spring Data JPA secara dinamis membuat
//    implementasi konkret di runtime berdasarkan kontrak method pencarian.
// =========================================================================
import com.example.demo.entity.Penjual;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PenjualRepository extends JpaRepository<Penjual, Long> {
    Optional<Penjual> findByEmail(String email);
    Optional<Penjual> findByGoogleId(String googleId);
}
