package com.example.demo;

import jakarta.persistence.*;

// Entitas JPA untuk tabel 'pusat'
@Entity
@Table(name = "pusat")
public class Pusat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pusat")
    private Long idPusat;

    @Column(name = "nama_gudang", nullable = false)
    private String namaGudang;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String nama;

    @Column(name = "no_telepon", unique = true)
    private String noTelepon;

    private String alamat;

    @Column(nullable = false)
    private String role = "PUSAT";

    public Long getIdPusat() { return idPusat; }
    public void setIdPusat(Long idPusat) { this.idPusat = idPusat; }
    public String getNamaGudang() { return namaGudang; }
    public void setNamaGudang(String namaGudang) { this.namaGudang = namaGudang; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
