package com.example.demo.entity;

import jakarta.persistence.*;

// Entitas JPA untuk tabel 'produk_olahan' (gambar mendukung Base64 TEXT maupun URL)
@Entity
@Table(name = "produk_olahan")
public class ProdukOlahan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produk")
    private Long idProduk;

    @Column(name = "nama_produk", nullable = false)
    private String namaProduk;

    private String deskripsi;

    @Column(name = "harga_poin", nullable = false)
    private Double hargaPoin;

    @Column(nullable = false)
    private Integer stok;

    @Column(columnDefinition = "TEXT")
    private String gambar;

    @Column(name = "pusat_id")
    private Long pusatId = 1L;

    public Long getIdProduk() { return idProduk; }
    public void setIdProduk(Long idProduk) { this.idProduk = idProduk; }
    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public Double getHargaPoin() { return hargaPoin; }
    public void setHargaPoin(Double hargaPoin) { this.hargaPoin = hargaPoin; }
    public Integer getStok() { return stok; }
    public void setStok(Integer stok) { this.stok = stok; }
    public String getGambar() { return gambar; }
    public void setGambar(String gambar) { this.gambar = gambar; }
    public Long getPusatId() { return pusatId; }
    public void setPusatId(Long pusatId) { this.pusatId = pusatId; }
}
