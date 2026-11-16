package com.example.demo;

import jakarta.persistence.*;

// Entitas JPA untuk tabel 'petugas'
@Entity
@Table(name = "petugas")
public class Petugas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_petugas")
    private Long idPetugas;

    @Column(name = "nama_petugas", nullable = false)
    private String namaPetugas;

    @Column(name = "no_telp", nullable = false, unique = true)
    private String noTelp;

    @Column(nullable = false)
    private String status = "tersedia";

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String alamat;

    @Column(nullable = false)
    private String role = "PETUGAS";

    @Column(name = "beban_angkut", nullable = false)
    private Double bebanAngkut = 0.0;

    @Column(name = "kapasitas_maksimal", nullable = false)
    private Double kapasitasMaksimal = 200.0;

    @Column(name = "tipe_aktivitas")
    private String tipeAktivitas; // null = bebas, "PENJUALAN" = sedang tugas jual minyak, "REDEEM" = sedang tugas antar redeem

    public Long getIdPetugas() { return idPetugas; }
    public void setIdPetugas(Long idPetugas) { this.idPetugas = idPetugas; }
    public String getNamaPetugas() { return namaPetugas; }
    public void setNamaPetugas(String namaPetugas) { this.namaPetugas = namaPetugas; }
    public String getNoTelp() { return noTelp; }
    public void setNoTelp(String noTelp) { this.noTelp = noTelp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Double getBebanAngkut() { return bebanAngkut; }
    public void setBebanAngkut(Double bebanAngkut) { this.bebanAngkut = bebanAngkut; }
    public Double getKapasitasMaksimal() { return kapasitasMaksimal; }
    public void setKapasitasMaksimal(Double kapasitasMaksimal) { this.kapasitasMaksimal = kapasitasMaksimal; }
    public String getTipeAktivitas() { return tipeAktivitas; }
    public void setTipeAktivitas(String tipeAktivitas) { this.tipeAktivitas = tipeAktivitas; }
}
