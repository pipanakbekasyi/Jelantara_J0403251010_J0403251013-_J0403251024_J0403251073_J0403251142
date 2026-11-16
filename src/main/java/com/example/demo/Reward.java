package com.example.demo;

import jakarta.persistence.*;

// Entitas JPA untuk tabel 'reward'
@Entity
@Table(name = "reward")
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reward")
    private Long idReward;

    @Column(name = "nama_reward", nullable = false)
    private String namaReward;

    private String deskripsi;

    @Column(name = "jumlah_poin", nullable = false)
    private Integer jumlahPoin;

    @Column(name = "penjualan_id", unique = true)
    private Long penjualanId;

    public Long getIdReward() { return idReward; }
    public void setIdReward(Long idReward) { this.idReward = idReward; }
    public String getNamaReward() { return namaReward; }
    public void setNamaReward(String namaReward) { this.namaReward = namaReward; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public Integer getJumlahPoin() { return jumlahPoin; }
    public void setJumlahPoin(Integer jumlahPoin) { this.jumlahPoin = jumlahPoin; }
    public Long getPenjualanId() { return penjualanId; }
    public void setPenjualanId(Long penjualanId) { this.penjualanId = penjualanId; }
}
