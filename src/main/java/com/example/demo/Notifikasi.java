package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;

// Entitas JPA untuk tabel 'notifikasi'
@Entity
@Table(name = "notifikasi")
public class Notifikasi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notifikasi")
    private Long idNotifikasi;

    @Column(nullable = false)
    private String judul;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pesan;

    @Column(nullable = false)
    private LocalDate tanggal = LocalDate.now();

    @Column(name = "status_baca", nullable = false)
    private Boolean statusBaca = false;

    @Column(nullable = false)
    private String tipe;

    @Column(name = "penjual_id")
    private Long penjualId;

    @Column(name = "petugas_id")
    private Long petugasId;

    @Column(name = "pusat_id")
    private Long pusatId;

    public Long getIdNotifikasi() { return idNotifikasi; }
    public void setIdNotifikasi(Long idNotifikasi) { this.idNotifikasi = idNotifikasi; }
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    public String getPesan() { return pesan; }
    public void setPesan(String pesan) { this.pesan = pesan; }
    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    public Boolean getStatusBaca() { return statusBaca; }
    public void setStatusBaca(Boolean statusBaca) { this.statusBaca = statusBaca; }
    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }
    public Long getPenjualId() { return penjualId; }
    public void setPenjualId(Long penjualId) { this.penjualId = penjualId; }
    public Long getPetugasId() { return petugasId; }
    public void setPetugasId(Long petugasId) { this.petugasId = petugasId; }
    public Long getPusatId() { return pusatId; }
    public void setPusatId(Long pusatId) { this.pusatId = pusatId; }
}
