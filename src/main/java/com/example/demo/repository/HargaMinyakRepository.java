package com.example.demo.repository;

// =========================================================================
// KONSEP OOP PADA INTERFACE INI:
// 1. INHERITANCE (Pewarisan Interface): Mewarisi seluruh operasi CRUD standar
//    (save, findById, findAll, delete) dari JpaRepository<HargaMinyak, Long>.
// 2. POLYMORPHISM (Polimorfisme): Spring Data JPA secara dinamis menghasilkan
//    implementasi proxy konkret di runtime untuk mengambil harga terbaru.
// =========================================================================
import com.example.demo.entity.HargaMinyak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HargaMinyakRepository extends JpaRepository<HargaMinyak, Long> {
    Optional<HargaMinyak> findTopByOrderByBerlakuMulaiDesc();
}
