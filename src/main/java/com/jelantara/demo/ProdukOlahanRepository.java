package com.jelantara.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdukOlahanRepository extends JpaRepository<ProdukOlahan, String> {

    // Fungsi untuk mencari daftar produk berdasarkan kata kunci nama produk (case-insensitive)
    List<ProdukOlahan> findByNamaProdukContainingIgnoreCase(String keyword);
}
