package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

// Entitas JPA untuk tabel 'penugasan'
@Entity
@Table(name = "penugasan")
public class Penugasan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penugasan")
    private Long idPenugasan;

    @Column(name = "tanggal_penugasan", nullable = false)
    private LocalDate tanggalPenugasan = LocalDate.now();

    @Column(name = "lokasi_penjual", nullable = false)
    private String lokasiPenjual;

    @Column(name = "pesan_penugasan", columnDefinition = "TEXT")
    private String pesanPenugasan;

    @Column(name = "status_penugasan", nullable = false)
    private String statusPenugasan = "Penugasan Kurir";

    @Column(name = "pusat_id")
    private Long pusatId = 1L;

    @Column(name = "petugas_id", nullable = false)
    private Long petugasId;

    @Column(name = "penjualan_id", unique = true)
    private Long penjualanId;

    public Long getIdPenugasan() { return idPenugasan; }
    public void setIdPenugasan(Long idPenugasan) { this.idPenugasan = idPenugasan; }
    public LocalDate getTanggalPenugasan() { return tanggalPenugasan; }
    public void setTanggalPenugasan(LocalDate tanggalPenugasan) { this.tanggalPenugasan = tanggalPenugasan; }
    public String getLokasiPenjual() { return lokasiPenjual; }
    public void setLokasiPenjual(String lokasiPenjual) { this.lokasiPenjual = lokasiPenjual; }
    public String getPesanPenugasan() { return pesanPenugasan; }
    public void setPesanPenugasan(String pesanPenugasan) { this.pesanPenugasan = pesanPenugasan; }
    public String getStatusPenugasan() { return statusPenugasan; }
    public void setStatusPenugasan(String statusPenugasan) { this.statusPenugasan = statusPenugasan; }
    public Long getPusatId() { return pusatId; }
    public void setPusatId(Long pusatId) { this.pusatId = pusatId; }
    public Long getPetugasId() { return petugasId; }
    public void setPetugasId(Long petugasId) { this.petugasId = petugasId; }
    public Long getPenjualanId() { return penjualanId; }
    public void setPenjualanId(Long penjualanId) { this.penjualanId = penjualanId; }
}
