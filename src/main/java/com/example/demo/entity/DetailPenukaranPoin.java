package com.example.demo.entity;

// =========================================================================
// KONSEP OOP PADA KELAS INI:
// 1. ENCAPSULATION (Enkapsulasi): Seluruh atribut item penukaran poin
//    dideklarasikan sebagai 'private' dengan public getter/setter.
// 2. COMPOSITION (Komposisi): Merupakan bagian komponen rincian produk
//    yang melekat pada transaksi induk PenukaranPoin (mendukung multi-produk).
// =========================================================================
import jakarta.persistence.*;

@Entity
@Table(name = "detail_penukaran_poin")
public class DetailPenukaranPoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detail")
    private Long idDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_penukaran", nullable = false)
    private PenukaranPoin penukaranPoin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_produk", nullable = false)
    private ProdukOlahan produkOlahan;

    @Column(name = "jumlah_item", nullable = false)
    private Integer jumlahItem = 1;

    @Column(name = "harga_poin_satuan", nullable = false)
    private Integer hargaPoinSatuan;

    @Column(name = "subtotal_poin", nullable = false)
    private Integer subtotalPoin;

    public DetailPenukaranPoin() {}

    public Long getIdDetail() { return idDetail; }
    public void setIdDetail(Long idDetail) { this.idDetail = idDetail; }

    public PenukaranPoin getPenukaranPoin() { return penukaranPoin; }
    public void setPenukaranPoin(PenukaranPoin penukaranPoin) { this.penukaranPoin = penukaranPoin; }

    public ProdukOlahan getProdukOlahan() { return produkOlahan; }
    public void setProdukOlahan(ProdukOlahan produkOlahan) { this.produkOlahan = produkOlahan; }

    public Integer getJumlahItem() { return jumlahItem; }
    public void setJumlahItem(Integer jumlahItem) { this.jumlahItem = jumlahItem; }

    public Integer getHargaPoinSatuan() { return hargaPoinSatuan; }
    public void setHargaPoinSatuan(Integer hargaPoinSatuan) { this.hargaPoinSatuan = hargaPoinSatuan; }

    public Integer getSubtotalPoin() { return subtotalPoin; }
    public void setSubtotalPoin(Integer subtotalPoin) { this.subtotalPoin = subtotalPoin; }
}
