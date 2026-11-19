package com.example.demo.entity;

// =========================================================================
// KONSEP OOP PADA KELAS INI:
// 1. ENCAPSULATION (Enkapsulasi): Seluruh atribut histori harga minyak
//    (idHarga, hargaPerLiter, berlakuMulai, pusat) dibungkus dengan modifier
//    'private' dan hanya dapat diakses melalui method public getter/setter.
// 2. ASSOCIATION (Many-to-One): Menghubungkan entitas HargaMinyak dengan
//    entitas Pusat sebagai pihak yang menetapkan harga acuan.
// =========================================================================
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "harga_minyak")
public class HargaMinyak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_harga")
    private Long idHarga;

    @Column(name = "harga_per_liter", nullable = false)
    private Double hargaPerLiter;

    @Column(name = "berlaku_mulai", nullable = false)
    private LocalDateTime berlakuMulai = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pusat")
    private Pusat pusat;

    public HargaMinyak() {}

    public HargaMinyak(Double hargaPerLiter, LocalDateTime berlakuMulai, Pusat pusat) {
        this.hargaPerLiter = hargaPerLiter;
        this.berlakuMulai = berlakuMulai;
        this.pusat = pusat;
    }

    public Long getIdHarga() { return idHarga; }
    public void setIdHarga(Long idHarga) { this.idHarga = idHarga; }

    public Double getHargaPerLiter() { return hargaPerLiter; }
    public void setHargaPerLiter(Double hargaPerLiter) { this.hargaPerLiter = hargaPerLiter; }

    public LocalDateTime getBerlakuMulai() { return berlakuMulai; }
    public void setBerlakuMulai(LocalDateTime berlakuMulai) { this.berlakuMulai = berlakuMulai; }

    public Pusat getPusat() { return pusat; }
    public void setPusat(Pusat pusat) { this.pusat = pusat; }
}
