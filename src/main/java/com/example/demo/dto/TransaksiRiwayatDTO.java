package com.example.demo.dto;

// =========================================================================
// KONSEP OOP PADA DTO INI:
// 1. ENCAPSULATION (Enkapsulasi): Membungkus atribut gabungan riwayat
//    transaksi (Jual Minyak & Redeem Poin) dengan akses private serta
//    public getter/setter untuk transfer data yang aman ke layer View.
// =========================================================================

import java.time.LocalDate;

// DTO untuk menyatukan riwayat transaksi jual minyak dan penukaran poin dalam satu tampilan terpadu
public class TransaksiRiwayatDTO {
    private String jenis; // "JUAL_MINYAK" atau "REDEEM"
    private String jenisLabel; // "Jual Minyak" atau "Redeem Poin"
    private Long id;
    private String kodeResi;
    private LocalDate tanggal;
    private String namaPenjual;
    private String namaPetugas;
    private String status;
    private String detailItem;
    private Double totalBayar;
    private Double poin;
    private String alamat;
    private String catatan;
    private String keteranganBukti;
    private String foto;
    private Double liter;
    private Double hargaSatuan;

    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }
    public String getJenisLabel() { return jenisLabel; }
    public void setJenisLabel(String jenisLabel) { this.jenisLabel = jenisLabel; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKodeResi() { return kodeResi; }
    public void setKodeResi(String kodeResi) { this.kodeResi = kodeResi; }
    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }
    public String getNamaPenjual() { return namaPenjual; }
    public void setNamaPenjual(String namaPenjual) { this.namaPenjual = namaPenjual; }
    public String getNamaPetugas() { return namaPetugas; }
    public void setNamaPetugas(String namaPetugas) { this.namaPetugas = namaPetugas; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDetailItem() { return detailItem; }
    public void setDetailItem(String detailItem) { this.detailItem = detailItem; }
    public Double getTotalBayar() { return totalBayar; }
    public void setTotalBayar(Double totalBayar) { this.totalBayar = totalBayar; }
    public Double getPoin() { return poin; }
    public void setPoin(Double poin) { this.poin = poin; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public String getKeteranganBukti() { return keteranganBukti; }
    public void setKeteranganBukti(String keteranganBukti) { this.keteranganBukti = keteranganBukti; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public Double getLiter() { return liter; }
    public void setLiter(Double liter) { this.liter = liter; }
    public Double getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(Double hargaSatuan) { this.hargaSatuan = hargaSatuan; }
}
