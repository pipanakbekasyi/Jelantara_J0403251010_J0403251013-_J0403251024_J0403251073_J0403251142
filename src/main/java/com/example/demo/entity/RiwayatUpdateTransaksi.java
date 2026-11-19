package com.example.demo.entity;

// =========================================================================
// KONSEP OOP PADA KELAS INI:
// 1. ENCAPSULATION (Enkapsulasi): Seluruh field log riwayat update transaksi
//    dideklarasikan sebagai 'private' agar integritas catatan historis dan
//    bukti foto tidak tertimpa dan hanya diakses melalui getter/setter.
// 2. IMMUTABLE AUDIT LOG DESIGN: Setiap perubahan status transaksi menghasilkan
//    baris baru (row baru) sehingga seluruh riwayat catatan dan bukti foto
//    tersimpan utuh untuk ditampilkan pada detail maupun template PDF.
// =========================================================================
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "riwayat_update_transaksi")
public class RiwayatUpdateTransaksi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_riwayat")
    private Long idRiwayat;

    @Column(name = "jenis_transaksi", nullable = false, length = 30)
    private String jenisTransaksi; // "JUAL_MINYAK" atau "REDEEM"

    @Column(name = "penjualan_id")
    private Long penjualanId;

    @Column(name = "penukaran_id")
    private Long penukaranId;

    @Column(nullable = false, length = 100)
    private String status;

    @Column(name = "aktor_role", nullable = false, length = 50)
    private String aktorRole; // "PENJUAL", "PETUGAS", "PUSAT"

    @Column(name = "nama_aktor", length = 150)
    private String namaAktor;

    @Column(name = "keterangan_bukti", columnDefinition = "TEXT")
    private String keteranganBukti;

    @Column(name = "foto_bukti", columnDefinition = "TEXT")
    private String fotoBukti;

    @Column(name = "waktu_update", nullable = false)
    private LocalDateTime waktuUpdate = LocalDateTime.now();

    public String getWaktuFormatted() {
        if (waktuUpdate == null) return "-";
        return waktuUpdate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }

    public Long getIdRiwayat() { return idRiwayat; }
    public void setIdRiwayat(Long idRiwayat) { this.idRiwayat = idRiwayat; }
    public String getJenisTransaksi() { return jenisTransaksi; }
    public void setJenisTransaksi(String jenisTransaksi) { this.jenisTransaksi = jenisTransaksi; }
    public Long getPenjualanId() { return penjualanId; }
    public void setPenjualanId(Long penjualanId) { this.penjualanId = penjualanId; }
    public Long getPenukaranId() { return penukaranId; }
    public void setPenukaranId(Long penukaranId) { this.penukaranId = penukaranId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAktorRole() { return aktorRole; }
    public void setAktorRole(String aktorRole) { this.aktorRole = aktorRole; }
    public String getNamaAktor() { return namaAktor; }
    public void setNamaAktor(String namaAktor) { this.namaAktor = namaAktor; }
    public String getKeteranganBukti() { return keteranganBukti; }
    public void setKeteranganBukti(String keteranganBukti) { this.keteranganBukti = keteranganBukti; }
    public String getFotoBukti() { return fotoBukti; }
    public void setFotoBukti(String fotoBukti) { this.fotoBukti = fotoBukti; }
    public LocalDateTime getWaktuUpdate() { return waktuUpdate; }
    public void setWaktuUpdate(LocalDateTime waktuUpdate) { this.waktuUpdate = waktuUpdate; }
}
