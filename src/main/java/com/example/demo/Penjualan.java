package com.example.demo;

import jakarta.persistence.*;
import java.time.LocalDate;

// Entitas JPA untuk tabel 'penjualan' dengan metode pencairan Bank/E-Wallet, status 5 tahap & bukti update
@Entity
@Table(name = "penjualan")
public class Penjualan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaksi")
    private Long idTransaksi;

    @Column(name = "tanggal_transaksi", nullable = false)
    private LocalDate tanggalTransaksi = LocalDate.now();

    @Column(name = "jumlah_minyak", nullable = false)
    private Double jumlahMinyak;

    @Column(name = "lokasi_penjemputan", nullable = false)
    private String lokasiPenjemputan;

    private String catatan;

    @Column(name = "metode_pembayaran")
    private String metodePembayaran = "Bank"; // "Bank" atau "E-Wallet"

    @Column(name = "nama_penyedia")
    private String namaPenyedia = "BCA"; // "BRI", "Mandiri", "BNI", "BCA", "GoPay", "DANA"

    @Column(name = "no_rekening_tujuan")
    private String noRekeningTujuan;

    @Column(columnDefinition = "TEXT")
    private String foto;

    @Column(name = "keterangan_bukti", columnDefinition = "TEXT")
    private String keteranganBukti;

    @Column(nullable = false)
    private String status = "Transaksi Masuk";

    @Column(name = "hasil_point")
    private Double hasilPoint;

    @Column(name = "total_harga")
    private Double totalHarga;

    @Column(name = "harga_minyak", nullable = false)
    private Double hargaMinyak;

    @Column(name = "penjual_id", nullable = false)
    private Long penjualId;

    @Column(name = "petugas_id")
    private Long petugasId;

    @Column(name = "pusat_id")
    private Long pusatId = 1L;

    @Transient
    private String namaPenjualTransient;

    @Transient
    private String namaPetugasTransient;

    public String getKodeResi() {
        return String.format("JLT-%06d", idTransaksi != null ? idTransaksi : 0);
    }

    public String getInfoRekeningLengkap() {
        if (noRekeningTujuan == null || noRekeningTujuan.isBlank()) return "-";
        return (metodePembayaran != null ? metodePembayaran : "Bank") + " - "
                + (namaPenyedia != null ? namaPenyedia : "BCA") + " (" + noRekeningTujuan + ")";
    }

    public int getTahapTimeline() {
        if (status == null) return 1;
        return switch (status.toUpperCase()) {
            case "TRANSAKSI MASUK" -> 1;
            case "PENUGASAN KURIR" -> 2;
            case "PENJEMPUTAN" -> 3;
            case "DIVERIFIKASI PETUGAS" -> 4;
            case "SELESAI" -> 5;
            default -> 1;
        };
    }

    public Long getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(Long idTransaksi) { this.idTransaksi = idTransaksi; }
    public LocalDate getTanggalTransaksi() { return tanggalTransaksi; }
    public void setTanggalTransaksi(LocalDate tanggalTransaksi) { this.tanggalTransaksi = tanggalTransaksi; }
    public Double getJumlahMinyak() { return jumlahMinyak; }
    public void setJumlahMinyak(Double jumlahMinyak) { this.jumlahMinyak = jumlahMinyak; }
    public String getLokasiPenjemputan() { return lokasiPenjemputan; }
    public void setLokasiPenjemputan(String lokasiPenjemputan) { this.lokasiPenjemputan = lokasiPenjemputan; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    public String getNamaPenyedia() { return namaPenyedia; }
    public void setNamaPenyedia(String namaPenyedia) { this.namaPenyedia = namaPenyedia; }
    public String getNoRekeningTujuan() { return noRekeningTujuan; }
    public void setNoRekeningTujuan(String noRekeningTujuan) { this.noRekeningTujuan = noRekeningTujuan; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public String getKeteranganBukti() { return keteranganBukti; }
    public void setKeteranganBukti(String keteranganBukti) { this.keteranganBukti = keteranganBukti; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getHasilPoint() { return hasilPoint; }
    public void setHasilPoint(Double hasilPoint) { this.hasilPoint = hasilPoint; }
    public Double getTotalHarga() { return totalHarga; }
    public void setTotalHarga(Double totalHarga) { this.totalHarga = totalHarga; }
    public Double getHargaMinyak() { return hargaMinyak; }
    public void setHargaMinyak(Double hargaMinyak) { this.hargaMinyak = hargaMinyak; }
    public Long getPenjualId() { return penjualId; }
    public void setPenjualId(Long penjualId) { this.penjualId = penjualId; }
    public Long getPetugasId() { return petugasId; }
    public void setPetugasId(Long petugasId) { this.petugasId = petugasId; }
    public Long getPusatId() { return pusatId; }
    public void setPusatId(Long pusatId) { this.pusatId = pusatId; }
    public String getNamaPenjualTransient() { return namaPenjualTransient; }
    public void setNamaPenjualTransient(String namaPenjualTransient) { this.namaPenjualTransient = namaPenjualTransient; }
    public String getNamaPetugasTransient() { return namaPetugasTransient; }
    public void setNamaPetugasTransient(String namaPetugasTransient) { this.namaPetugasTransient = namaPetugasTransient; }
}
