package com.example.demo.repository;

// =========================================================================
// KONSEP OOP PADA INTERFACE INI:
// 1. INHERITANCE (Pewarisan Interface): Mewarisi operasi persistensi data
//    dari JpaRepository<DetailPenukaranPoin, Long>.
// 2. ABSTRACTION (Abstraksi): Menyembunyikan query SQL relasional di balik
//    kontrak method Java untuk mencari daftar produk pada satu transaksi redeem.
// =========================================================================
import com.example.demo.entity.DetailPenukaranPoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailPenukaranPoinRepository extends JpaRepository<DetailPenukaranPoin, Long> {
    List<DetailPenukaranPoin> findByPenukaranPoinIdPenukaran(Long idPenukaran);
}
