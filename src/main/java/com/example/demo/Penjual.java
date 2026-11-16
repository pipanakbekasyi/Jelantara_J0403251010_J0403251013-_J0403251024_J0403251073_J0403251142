package com.example.demo;

import jakarta.persistence.*;

// Entitas JPA untuk tabel 'penjual' dengan dukungan terpisah Metode Pembayaran (Bank/E-Wallet), Nama Penyedia, & Nomor Rekening
@Entity
@Table(name = "penjual")
public class Penjual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_penjual")
    private Long idPenjual;

    @Column(name = "nama_penjual", nullable = false)
    private String namaPenjual;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "no_hp", nullable = false, unique = true)
    private String noHp;

    private String alamat;

    @Column(nullable = false)
    private Integer poin = 0;

    @Column(name = "metode_pembayaran")
    private String metodePembayaran = "Bank"; // "Bank" atau "E-Wallet"

    @Column(name = "nama_penyedia")
    private String namaPenyedia = "BCA"; // "BRI", "Mandiri", "BNI", "BCA" atau "GoPay", "DANA"

    @Column(name = "no_rekening")
    private String noRekening; // Nomor Rekening atau Nomor E-Wallet

    @Column(name = "kategori_penjual")
    private String kategoriPenjual = "Rumah Tangga";

    @Column(nullable = false)
    private String role = "PENJUAL";

    public String getInfoPembayaranLengkap() {
        if (noRekening == null || noRekening.isBlank()) return "-";
        String metode = (metodePembayaran != null) ? metodePembayaran : "Bank";
        String provider = (namaPenyedia != null) ? namaPenyedia : "BCA";
        return metode + " - " + provider + " (" + noRekening + ")";
    }

    public Long getIdPenjual() { return idPenjual; }
    public void setIdPenjual(Long idPenjual) { this.idPenjual = idPenjual; }
    public String getNamaPenjual() { return namaPenjual; }
    public void setNamaPenjual(String namaPenjual) { this.namaPenjual = namaPenjual; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNoHp() { return noHp; }
    public void setNoHp(String noHp) { this.noHp = noHp; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public Integer getPoin() { return poin; }
    public void setPoin(Integer poin) { this.poin = poin; }
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    public String getNamaPenyedia() { return namaPenyedia; }
    public void setNamaPenyedia(String namaPenyedia) { this.namaPenyedia = namaPenyedia; }
    public String getNoRekening() { return noRekening; }
    public void setNoRekening(String noRekening) { this.noRekening = noRekening; }
    public String getKategoriPenjual() { return kategoriPenjual; }
    public void setKategoriPenjual(String kategoriPenjual) { this.kategoriPenjual = kategoriPenjual; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
