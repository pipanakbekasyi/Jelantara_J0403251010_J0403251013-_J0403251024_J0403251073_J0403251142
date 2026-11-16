package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;

// Entitas JPA untuk tabel 'stok_minyak'
@Entity
@Table(name = "stok_minyak")
public class StokMinyak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stok")
    private Long idStok;

    @Column(name = "jumlah_minyak", nullable = false)
    private Double jumlahMinyak = 0.0;

    @Column(name = "batas_stok", nullable = false)
    private Double batasStok = 1000.0;

    @Column(name = "jumlah_update", nullable = false)
    private Double jumlahUpdate = 0.0;

    @Column(name = "tgl_update", nullable = false)
    private LocalDate tglUpdate = LocalDate.now();

    @Column(name = "penjualan_id")
    private Long penjualanId;

    @Column(name = "pusat_id")
    private Long pusatId = 1L;

    public double getPersentaseKapasitas() {
        if (batasStok == null || batasStok <= 0) return 0;
        return Math.min(100.0, Math.round((jumlahUpdate / batasStok) * 1000.0) / 10.0);
    }

    public Long getIdStok() { return idStok; }
    public void setIdStok(Long idStok) { this.idStok = idStok; }
    public Double getJumlahMinyak() { return jumlahMinyak; }
    public void setJumlahMinyak(Double jumlahMinyak) { this.jumlahMinyak = jumlahMinyak; }
    public Double getBatasStok() { return batasStok; }
    public void setBatasStok(Double batasStok) { this.batasStok = batasStok; }
    public Double getJumlahUpdate() { return jumlahUpdate; }
    public void setJumlahUpdate(Double jumlahUpdate) { this.jumlahUpdate = jumlahUpdate; }
    public LocalDate getTglUpdate() { return tglUpdate; }
    public void setTglUpdate(LocalDate tglUpdate) { this.tglUpdate = tglUpdate; }
    public Long getPenjualanId() { return penjualanId; }
    public void setPenjualanId(Long penjualanId) { this.penjualanId = penjualanId; }
    public Long getPusatId() { return pusatId; }
    public void setPusatId(Long pusatId) { this.pusatId = pusatId; }
}
