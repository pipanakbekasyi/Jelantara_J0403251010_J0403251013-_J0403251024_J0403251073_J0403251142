package com.example.demo.repository;

import com.example.demo.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface PenjualanRepository extends JpaRepository<Penjualan, Long> {
    List<Penjualan> findByPenjualIdOrderByIdTransaksiDesc(Long penjualId);
    List<Penjualan> findByPetugasIdOrderByIdTransaksiDesc(Long petugasId);
    List<Penjualan> findAllByOrderByIdTransaksiDesc();

    long countByTanggalTransaksi(LocalDate tanggal);

    @Query("SELECT COALESCE(SUM(p.jumlahMinyak), 0) FROM Penjualan p WHERE p.tanggalTransaksi = :tgl")
    Double sumJumlahMinyakByTanggal(@Param("tgl") LocalDate tgl);

    long countByPetugasIdAndTanggalTransaksi(Long petugasId, LocalDate tanggal);

    @Query("SELECT COALESCE(SUM(p.jumlahMinyak), 0) FROM Penjualan p WHERE p.petugasId = :ptgId AND p.tanggalTransaksi = :tgl")
    Double sumJumlahMinyakByPetugasAndTanggal(@Param("ptgId") Long petugasId, @Param("tgl") LocalDate tgl);
}
