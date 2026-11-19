package com.example.demo.repository;

import com.example.demo.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PenukaranPoinRepository extends JpaRepository<PenukaranPoin, Long> {
    List<PenukaranPoin> findByPenjualIdOrderByIdPenukaranDesc(Long penjualId);
    List<PenukaranPoin> findByPetugasIdOrderByIdPenukaranDesc(Long petugasId);
    List<PenukaranPoin> findAllByOrderByIdPenukaranDesc();

    // Menghitung jumlah permintaan redeem poin berdasarkan tanggal hari ini
    long countByTglPenukaran(LocalDate tgl);
}
