package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;

// Entitas JPA untuk tabel 'penukaran_poin' dengan tahapan flow redeem, kurir pengantaran & bukti foto
@Entity
@Table(name = "penukaran_poin")
public class PenukaranPoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penukaran")
    private Long idPenukaran;

    @Column(name = "jumlah_poin", nullable = false)
    private Double jumlahPoin;

    @Column(name = "alamat_pengantaran", nullable = false)
    private String alamatPengantaran;

    @Column(name = "harga_poin_satuan", nullable = false)
    private Integer hargaPoinSatuan;

    @Column(name = "tgl_penukaran", nullable = false)
    private LocalDate tglPenukaran = LocalDate.now();

    @Column(name = "jumlah_poin_sisa", nullable = false)
    private Double jumlahPoinSisa = 0.0;

    @Column(name = "jumlah_unit")
    private Integer jumlahUnit = 1;

    private String catatan;

    @Column(columnDefinition = "TEXT")
    private String foto;

    @Column(name = "keterangan_bukti", columnDefinition = "TEXT")
    private String keteranganBukti;

    @Column(nullable = false)
    private String status = "Request Masuk";

    @Column(name = "penjual_id", nullable = false)
    private Long penjualId;

    @Column(name = "produk_id", nullable = false)
    private Long produkId;

    @Column(name = "petugas_id")
    private Long petugasId;

    @Column(name = "pusat_id")
    private Long pusatId = 1L;

    @Transient
    private String namaPenjualTransient;

    @Transient
    private String namaProdukTransient;

    @Transient
    private String namaPetugasTransient;

    // Nomor resi penukaran poin RDM-00000X
    public String getKodeResi() {
        return String.format("RDM-%06d", idPenukaran != null ? idPenukaran : 0);
    }

    // 5 Tahapan Flow Redeem:
    // 1. Request Masuk
    // 2. Redeem Dikonfirmasi
    // 3. Petugas Ditugaskan
    // 4. Petugas Dalam Perjalanan
    // 5. Selesai
    public int getTahapTimeline() {
        if (status == null) return 1;
        return switch (status.toUpperCase()) {
            case "REQUEST MASUK" -> 1;
            case "REDEEM DIKONFIRMASI" -> 2;
            case "PETUGAS DITUGASKAN" -> 3;
            case "PETUGAS DALAM PERJALANAN" -> 4;
            case "SELESAI" -> 5;
            default -> 1;
        };
    }

    public Long getIdPenukaran() { return idPenukaran; }
    public void setIdPenukaran(Long idPenukaran) { this.idPenukaran = idPenukaran; }
    public Double getJumlahPoin() { return jumlahPoin; }
    public void setJumlahPoin(Double jumlahPoin) { this.jumlahPoin = jumlahPoin; }
    public String getAlamatPengantaran() { return alamatPengantaran; }
    public void setAlamatPengantaran(String alamatPengantaran) { this.alamatPengantaran = alamatPengantaran; }
    public Integer getHargaPoinSatuan() { return hargaPoinSatuan; }
    public void setHargaPoinSatuan(Integer hargaPoinSatuan) { this.hargaPoinSatuan = hargaPoinSatuan; }
    public LocalDate getTglPenukaran() { return tglPenukaran; }
    public void setTglPenukaran(LocalDate tglPenukaran) { this.tglPenukaran = tglPenukaran; }
    public Double getJumlahPoinSisa() { return jumlahPoinSisa; }
    public void setJumlahPoinSisa(Double jumlahPoinSisa) { this.jumlahPoinSisa = jumlahPoinSisa; }
    public Integer getJumlahUnit() { return jumlahUnit != null ? jumlahUnit : 1; }
    public void setJumlahUnit(Integer jumlahUnit) { this.jumlahUnit = jumlahUnit; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public String getKeteranganBukti() { return keteranganBukti; }
    public void setKeteranganBukti(String keteranganBukti) { this.keteranganBukti = keteranganBukti; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getPenjualId() { return penjualId; }
    public void setPenjualId(Long penjualId) { this.penjualId = penjualId; }
    public Long getProdukId() { return produkId; }
    public void setProdukId(Long produkId) { this.produkId = produkId; }
    public Long getPetugasId() { return petugasId; }
    public void setPetugasId(Long petugasId) { this.petugasId = petugasId; }
    public Long getPusatId() { return pusatId; }
    public void setPusatId(Long pusatId) { this.pusatId = pusatId; }
    public String getNamaPenjualTransient() { return namaPenjualTransient; }
    public void setNamaPenjualTransient(String namaPenjualTransient) { this.namaPenjualTransient = namaPenjualTransient; }
    public String getNamaProdukTransient() { return namaProdukTransient; }
    public void setNamaProdukTransient(String namaProdukTransient) { this.namaProdukTransient = namaProdukTransient; }
    public String getNamaPetugasTransient() { return namaPetugasTransient; }
    public void setNamaPetugasTransient(String namaPetugasTransient) { this.namaPetugasTransient = namaPetugasTransient; }
}
