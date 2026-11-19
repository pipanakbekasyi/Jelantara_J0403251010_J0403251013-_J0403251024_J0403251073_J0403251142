package com.example.demo.repository;

import com.example.demo.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Repository untuk mengakses notifikasi bagi Penjual, Petugas, dan Pusat
public interface NotifikasiRepository extends JpaRepository<Notifikasi, Long> {
    List<Notifikasi> findByPenjualIdOrderByIdNotifikasiDesc(Long penjualId);
    List<Notifikasi> findByPetugasIdOrderByIdNotifikasiDesc(Long petugasId);
    List<Notifikasi> findByPusatIdOrderByIdNotifikasiDesc(Long pusatId);
    List<Notifikasi> findAllByOrderByIdNotifikasiDesc();

    long countByPenjualIdAndStatusBacaFalse(Long penjualId);
    long countByPetugasIdAndStatusBacaFalse(Long petugasId);
    long countByPusatIdAndStatusBacaFalse(Long pusatId);
}
