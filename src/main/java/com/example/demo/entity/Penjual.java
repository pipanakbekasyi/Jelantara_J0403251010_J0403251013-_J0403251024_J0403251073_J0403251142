package com.example.demo.entity;

// =========================================================================
// KONSEP OOP PADA KELAS INI:
// 1. ENCAPSULATION (Enkapsulasi): Seluruh atribut entitas (idPenjual, email,
//    password, poin, noRekening, fotoProfil, authProvider, googleId)
//    dideklarasikan sebagai 'private' dan diakses via public getter/setter.
// 2. ABSTRACTION (Abstraksi): Menyediakan method bisnis tingkat tinggi seperti
//    getInfoPembayaranLengkap() yang menyembunyikan detail pengecekan null.
// =========================================================================
import jakarta.persistence.*;

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

    @Column(nullable = true)
    private String password;

    @Column(name = "no_hp", nullable = true, unique = true)
    private String noHp;

    private String alamat;

    @Column(nullable = false)
    private Integer poin = 0;

    @Column(name = "metode_pembayaran")
    private String metodePembayaran = "Bank";

    @Column(name = "nama_penyedia")
    private String namaPenyedia = "BCA";

    @Column(name = "no_rekening")
    private String noRekening;

    @Column(name = "kategori_penjual")
    private String kategoriPenjual = "Rumah Tangga";

    @Column(nullable = false)
    private String role = "PENJUAL";

    @Column(name = "foto_profil", columnDefinition = "TEXT")
    private String fotoProfil;

    @Column(name = "auth_provider", length = 50)
    private String authProvider = "LOCAL"; // "LOCAL" atau "GOOGLE"

    @Column(name = "google_id", unique = true)
    private String googleId;

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
    public String getFotoProfil() { return fotoProfil; }
    public void setFotoProfil(String fotoProfil) { this.fotoProfil = fotoProfil; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
}
