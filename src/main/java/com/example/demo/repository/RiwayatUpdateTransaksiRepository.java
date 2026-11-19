package com.example.demo.repository;

// =========================================================================
// KONSEP OOP PADA INTERFACE INI:
// 1. INHERITANCE (Pewarisan Interface): Mewarisi operasi CRUD standar dari
//    JpaRepository<RiwayatUpdateTransaksi, Long>.
// 2. POLYMORPHISM: Spring Data JPA membangun implementasi query secara dinamis
//    di runtime berdasarkan nama method kontrak.
// =========================================================================
import com.example.demo.entity.RiwayatUpdateTransaksi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiwayatUpdateTransaksiRepository extends JpaRepository<RiwayatUpdateTransaksi, Long> {
    List<RiwayatUpdateTransaksi> findByPenjualanIdOrderByWaktuUpdateAscIdRiwayatAsc(Long penjualanId);
    List<RiwayatUpdateTransaksi> findByPenukaranIdOrderByWaktuUpdateAscIdRiwayatAsc(Long penukaranId);
}
