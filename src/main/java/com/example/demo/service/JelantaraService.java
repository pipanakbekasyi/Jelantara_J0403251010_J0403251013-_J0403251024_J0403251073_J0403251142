package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import java.time.LocalDateTime;

// =========================================================================
// KONSEP OOP PADA SERVICE LAYER INI:
// 1. ABSTRACTION (Abstraksi): Mengenkapsulasi seluruh kompleksitas aturan
//    bisnis (validasi min. 15L, batas kapasitas tangki 80%, kalkulasi poin,
//    serta alur 5 tahap transaksi) ke dalam method layanan yang terstruktur.
// 2. COMPOSITION & DEPENDENCY INJECTION: Menggabungkan (has-a) berbagai
//    Repository (PenjualRepository, PetugasRepository, PenjualanRepository,
//    dll.) melalui Constructor Injection untuk menjalankan transaksi terpadu.
// 3. ENCAPSULATION: Helper internal seperti convertFileToBase64() dan
//    buatProdukAwal() disembunyikan dengan modifier 'private'.
// =========================================================================
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Kelas Service yang menangani proses bisnis Jelantara, transaksi jual minyak & redeem poin 5 tahap
@Service
public class JelantaraService {

    public record AuthSession(Long id, String nama, String email, String role) {}

    private final PenjualRepository penjualRepo;
    private final PetugasRepository petugasRepo;
    private final PusatRepository pusatRepo;
    private final PenjualanRepository penjualanRepo;
    private final PenugasanRepository penugasanRepo;
    private final StokMinyakRepository stokRepo;
    private final ProdukOlahanRepository produkRepo;
    private final PenukaranPoinRepository penukaranRepo;
    private final NotifikasiRepository notifikasiRepo;
    private final HargaMinyakRepository hargaMinyakRepo;
    private final DetailPenukaranPoinRepository detailPenukaranRepo;
    private final RiwayatUpdateTransaksiRepository riwayatUpdateRepo;
    private final PasswordEncoderService passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private Double hargaMinyakBerlaku = 6000.0;

    public JelantaraService(PenjualRepository penjualRepo, PetugasRepository petugasRepo,
                            PusatRepository pusatRepo, PenjualanRepository penjualanRepo,
                            PenugasanRepository penugasanRepo, StokMinyakRepository stokRepo,
                            ProdukOlahanRepository produkRepo, PenukaranPoinRepository penukaranRepo,
                            NotifikasiRepository notifikasiRepo, HargaMinyakRepository hargaMinyakRepo,
                            DetailPenukaranPoinRepository detailPenukaranRepo,
                            RiwayatUpdateTransaksiRepository riwayatUpdateRepo,
                            PasswordEncoderService passwordEncoder,
                            JdbcTemplate jdbcTemplate) {
        this.penjualRepo = penjualRepo;
        this.petugasRepo = petugasRepo;
        this.pusatRepo = pusatRepo;
        this.penjualanRepo = penjualanRepo;
        this.penugasanRepo = penugasanRepo;
        this.stokRepo = stokRepo;
        this.produkRepo = produkRepo;
        this.penukaranRepo = penukaranRepo;
        this.notifikasiRepo = notifikasiRepo;
        this.hargaMinyakRepo = hargaMinyakRepo;
        this.detailPenukaranRepo = detailPenukaranRepo;
        this.riwayatUpdateRepo = riwayatUpdateRepo;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    // Menghitung poin reward berdasarkan aturan kelipatan 15 Liter = 15 Poin (Min. 15 L)
    public double hitungPoinKelipatan15(Double liter) {
        if (liter == null || liter < 15.0) return 0.0;
        return Math.floor(liter / 15.0) * 15.0;
    }

    // Mengonversi MultipartFile menjadi Base64 Data URI agar foto kamera/upload langsung tersimpan di DB
    public String convertFileToBase64(MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
                String base64 = Base64.getEncoder().encodeToString(file.getBytes());
                return "data:" + contentType + ";base64," + base64;
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Menginisialisasi kolom baru secara otomatis & aman pada tabel existing serta mengisi seed data awal
    @PostConstruct
    public void initSeedData() {
        try {
            jdbcTemplate.execute("ALTER TABLE penjual ADD COLUMN IF NOT EXISTS kategori_penjual VARCHAR(100) DEFAULT 'Rumah Tangga'");
            jdbcTemplate.execute("ALTER TABLE penjual ADD COLUMN IF NOT EXISTS metode_pembayaran VARCHAR(50) DEFAULT 'Bank'");
            jdbcTemplate.execute("ALTER TABLE penjual ADD COLUMN IF NOT EXISTS nama_penyedia VARCHAR(50) DEFAULT 'BCA'");
            jdbcTemplate.execute("ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS metode_pembayaran VARCHAR(50) DEFAULT 'Bank'");
            jdbcTemplate.execute("ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS nama_penyedia VARCHAR(50) DEFAULT 'BCA'");
            jdbcTemplate.execute("ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS no_rekening_tujuan VARCHAR(100)");
            jdbcTemplate.execute("ALTER TABLE petugas ADD COLUMN IF NOT EXISTS tipe_aktivitas VARCHAR(50)");
            jdbcTemplate.execute("ALTER TABLE penjualan ADD COLUMN IF NOT EXISTS keterangan_bukti TEXT");
            jdbcTemplate.execute("ALTER TABLE penugasan ADD COLUMN IF NOT EXISTS pesan_penugasan TEXT");
            jdbcTemplate.execute("ALTER TABLE penukaran_poin ADD COLUMN IF NOT EXISTS jumlah_unit INTEGER DEFAULT 1");
            jdbcTemplate.execute("UPDATE penukaran_poin SET jumlah_unit = 1 WHERE jumlah_unit IS NULL");
            jdbcTemplate.execute("ALTER TABLE penukaran_poin ADD COLUMN IF NOT EXISTS catatan VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE penukaran_poin ADD COLUMN IF NOT EXISTS foto TEXT");
            jdbcTemplate.execute("ALTER TABLE penukaran_poin ADD COLUMN IF NOT EXISTS keterangan_bukti TEXT");
            jdbcTemplate.execute("ALTER TABLE penukaran_poin ADD COLUMN IF NOT EXISTS petugas_id BIGINT");
            jdbcTemplate.execute("ALTER TABLE penukaran_poin ALTER COLUMN produk_id DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE penjual ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(50) DEFAULT 'LOCAL'");
            jdbcTemplate.execute("ALTER TABLE penjual ADD COLUMN IF NOT EXISTS google_id VARCHAR(255)");
            jdbcTemplate.execute("ALTER TABLE penjual ADD COLUMN IF NOT EXISTS foto_profil TEXT");
            jdbcTemplate.execute("ALTER TABLE petugas ADD COLUMN IF NOT EXISTS foto_profil TEXT");
            jdbcTemplate.execute("ALTER TABLE penjual ALTER COLUMN password DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE penjual ALTER COLUMN no_hp DROP NOT NULL");
        } catch (Exception e) {
            System.out.println("Info Auto-Migrate Schema: " + e.getMessage());
        }
        try {
        if (pusatRepo.count() == 0) {
            Pusat p = new Pusat();
            p.setNamaGudang("Gudang Bandung");
            p.setNama("Admin Gudang Bandung");
            p.setEmail("pusat@mail.com");
            p.setPassword("pusat123");
            p.setNoTelepon("081122334455");
            p.setAlamat("Jl. Soekarno Hatta No. 99");
            pusatRepo.save(p);
        }
        if (petugasRepo.count() == 0) {
            Petugas pt = new Petugas();
            pt.setNamaPetugas("Andi Pratama");
            pt.setEmail("petugas@mail.com");
            pt.setPassword("petugas123");
            pt.setNoTelp("081234567890");
            pt.setAlamat("Jl. Merdeka No. 10");
            pt.setStatus("tersedia");
            pt.setBebanAngkut(0.0);
            pt.setKapasitasMaksimal(200.0);
            petugasRepo.save(pt);
        }
        if (penjualRepo.count() == 0) {
            Penjual pj = new Penjual();
            pj.setNamaPenjual("Budi Santoso");
            pj.setEmail("budi@gmail.com");
            pj.setPassword("penjual123");
            pj.setNoHp("081299887766");
            pj.setAlamat("Jl. Melati No. 10, Bogor");
            pj.setPoin(350);
            pj.setKategoriPenjual("Rumah Tangga");
            pj.setNoRekening("BCA 7210998212");
            penjualRepo.save(pj);
        }
        if (hargaMinyakRepo.count() == 0) {
            Pusat pAktif = pusatRepo.findAll().isEmpty() ? null : pusatRepo.findAll().get(0);
            hargaMinyakRepo.save(new HargaMinyak(6000.0, LocalDateTime.now(), pAktif));
        }
        if (stokRepo.count() == 0) {
            StokMinyak st = new StokMinyak();
            st.setJumlahMinyak(0.0);
            st.setJumlahUpdate(0.0);
            st.setBatasStok(1000.0);
            st.setPusatId(1L);
            stokRepo.save(st);
        }
        if (produkRepo.count() == 0) {
            buatProdukAwal("Sabun Cuci Piring", "Sabun cuci piring cair hasil olahan minyak jelantah", 150.0, 20, "https://placehold.co/300x200/FFF3B8/926008?text=Sabun+Piring");
            buatProdukAwal("Sabun Cuci Tangan", "Sabun cuci tangan gel hasil olahan minyak jelantah", 120.0, 15, "https://placehold.co/300x200/FFF3B8/926008?text=Sabun+Tangan");
            buatProdukAwal("Lilin Eco Jelantara", "Lilin tangkai ramah lingkungan dari minyak jelantah", 200.0, 10, "https://placehold.co/300x200/FFF3B8/926008?text=Lilin+Eco");
        }
        if (penjualanRepo.count() == 0) {
            Penjualan trx = new Penjualan();
            trx.setTanggalTransaksi(LocalDate.now());
            trx.setJumlahMinyak(15.0);
            trx.setLokasiPenjemputan("Jl. Melati No. 10, Bogor");
            trx.setCatatan("Minyak disimpan dalam jerigen tertutup");
            trx.setStatus("Transaksi Masuk");
            trx.setHargaMinyak(6000.0);
            trx.setTotalHarga(90000.0);
            trx.setHasilPoint(hitungPoinKelipatan15(15.0));
            trx.setPenjualId(1L);
            trx.setPusatId(1L);
            trx.setKeteranganBukti("Menunggu penugasan kurir dari Pusat.");
            penjualanRepo.save(trx);
        }
        if (penukaranRepo.count() == 0) {
            PenukaranPoin rdm = new PenukaranPoin();
            rdm.setPenjualId(1L);
            rdm.setProdukId(1L);
            rdm.setJumlahUnit(1);
            rdm.setHargaPoinSatuan(150);
            rdm.setJumlahPoin(150.0);
            rdm.setJumlahPoinSisa(200.0);
            rdm.setAlamatPengantaran("Jl. Melati No. 10, Bogor");
            rdm.setCatatan("Ditaruh di teras rumah");
            rdm.setStatus("Request Masuk");
            rdm.setKeteranganBukti("Permintaan redeem dibuat oleh Penjual. Menunggu konfirmasi dari Pusat.");
            rdm.setTglPenukaran(LocalDate.now());
            rdm.setPusatId(1L);
            penukaranRepo.save(rdm);
        }
        } catch (Exception e) {
            System.out.println("Info Seed Data: " + e.getMessage());
        }

        // Enkripsi otomatis seluruh password user (Pusat, Petugas, Penjual) yang masih plaintext atau null
        try {
            for (Pusat p : pusatRepo.findAll()) {
                if (p.getPassword() != null && !p.getPassword().isBlank() && !passwordEncoder.isEncoded(p.getPassword())) {
                    p.setPassword(passwordEncoder.encodePassword(p.getPassword()));
                    pusatRepo.save(p);
                }
            }
            for (Petugas pt : petugasRepo.findAll()) {
                if (pt.getPassword() != null && !pt.getPassword().isBlank() && !passwordEncoder.isEncoded(pt.getPassword())) {
                    pt.setPassword(passwordEncoder.encodePassword(pt.getPassword()));
                    petugasRepo.save(pt);
                }
            }
            for (Penjual pj : penjualRepo.findAll()) {
                if (pj.getPassword() == null || pj.getPassword().isBlank()) {
                    pj.setPassword(passwordEncoder.generateGooglePlaceholderPassword());
                    penjualRepo.save(pj);
                } else if (!passwordEncoder.isEncoded(pj.getPassword())) {
                    pj.setPassword(passwordEncoder.encodePassword(pj.getPassword()));
                    penjualRepo.save(pj);
                }
            }
        } catch (Exception e) {
            System.out.println("Info Enkripsi Password: " + e.getMessage());
        }
    }

    private void buatProdukAwal(String nama, String desk, Double poin, Integer stok, String img) {
        ProdukOlahan pr = new ProdukOlahan();
        pr.setNamaProduk(nama);
        pr.setDeskripsi(desk);
        pr.setHargaPoin(poin);
        pr.setStok(stok);
        pr.setGambar(img);
        pr.setPusatId(1L);
        produkRepo.save(pr);
    }

    // Autentikasi RBAC otomatis berdasarkan email & password
    public Optional<AuthSession> authenticateUser(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return Optional.empty();
        String cleanEmail = email.trim();

        Optional<Pusat> optPusat = pusatRepo.findByEmail(cleanEmail);
        if (optPusat.isPresent() && passwordEncoder.matchesPassword(password, optPusat.get().getPassword())) {
            Pusat p = optPusat.get();
            return Optional.of(new AuthSession(p.getIdPusat(), p.getNamaGudang(), p.getEmail(), "PUSAT"));
        }

        Optional<Petugas> optPetugas = petugasRepo.findByEmail(cleanEmail);
        if (optPetugas.isPresent() && passwordEncoder.matchesPassword(password, optPetugas.get().getPassword())) {
            Petugas pt = optPetugas.get();
            if ("NONAKTIF".equalsIgnoreCase(pt.getRole())) {
                return Optional.empty();
            }
            return Optional.of(new AuthSession(pt.getIdPetugas(), pt.getNamaPetugas(), pt.getEmail(), "PETUGAS"));
        }

        Optional<Penjual> optPenjual = penjualRepo.findByEmail(cleanEmail);
        if (optPenjual.isPresent() && optPenjual.get().getPassword() != null && passwordEncoder.matchesPassword(password, optPenjual.get().getPassword())) {
            Penjual pj = optPenjual.get();
            return Optional.of(new AuthSession(pj.getIdPenjual(), pj.getNamaPenjual(), pj.getEmail(), "PENJUAL"));
        }

        return Optional.empty();
    }

    // Metrik Overview Hari Ini untuk POV Pusat
    public long getPermintaanRedeemHariIni() {
        return penukaranRepo.countByTglPenukaran(LocalDate.now());
    }

    public long getPermintaanTransaksiHariIni() {
        return penjualanRepo.countByTanggalTransaksi(LocalDate.now());
    }

    public long getTotalPetugasAvail() {
        return petugasRepo.countByStatusIgnoreCase("tersedia");
    }

    public Double getBanyakMinyakMasukHariIni() {
        return penjualanRepo.sumJumlahMinyakByTanggal(LocalDate.now());
    }

    public Penjual getPenjualAktif(Long penjualId) {
        if (penjualId != null) {
            return penjualRepo.findById(penjualId).orElseGet(() -> penjualRepo.findAll().get(0));
        }
        return penjualRepo.findAll().get(0);
    }

    public Petugas getPetugasAktif(Long petugasId) {
        if (petugasId != null) {
            return petugasRepo.findById(petugasId).orElseGet(() -> petugasRepo.findAll().get(0));
        }
        return petugasRepo.findAll().get(0);
    }

    public Pusat getPusatAktif() {
        return pusatRepo.findAll().get(0);
    }

    public StokMinyak getStokTerbaru() {
        return stokRepo.findTopByOrderByIdStokDesc().orElseGet(() -> {
            StokMinyak s = new StokMinyak();
            s.setBatasStok(1000.0);
            s.setJumlahUpdate(0.0);
            return s;
        });
    }

    public Double getHargaMinyakBerlaku() {
        return hargaMinyakRepo.findTopByOrderByBerlakuMulaiDesc()
                .map(HargaMinyak::getHargaPerLiter)
                .orElse(this.hargaMinyakBerlaku);
    }

    @Transactional
    public void updateHargaMinyakOlehPusat(Double hargaBaru) {
        if (hargaBaru != null && hargaBaru > 0) {
            this.hargaMinyakBerlaku = hargaBaru;
            Pusat pusatAktif = pusatRepo.findAll().isEmpty() ? null : pusatRepo.findAll().get(0);
            hargaMinyakRepo.save(new HargaMinyak(hargaBaru, LocalDateTime.now(), pusatAktif));
        }
    }

    // Melengkapi nama penjual dan nama petugas pada setiap objek Penjualan
    public List<Penjualan> enrichPenjualanList(List<Penjualan> list) {
        Map<Long, String> mapPenjual = penjualRepo.findAll().stream()
                .collect(Collectors.toMap(Penjual::getIdPenjual, Penjual::getNamaPenjual, (a, b) -> a));
        Map<Long, String> mapPetugas = petugasRepo.findAll().stream()
                .collect(Collectors.toMap(Petugas::getIdPetugas, Petugas::getNamaPetugas, (a, b) -> a));

        for (Penjualan p : list) {
            p.setNamaPenjualTransient(mapPenjual.getOrDefault(p.getPenjualId(), "Penjual #" + p.getPenjualId()));
            if (p.getPetugasId() != null) {
                p.setNamaPetugasTransient(mapPetugas.getOrDefault(p.getPetugasId(), "Petugas #" + p.getPetugasId()));
            } else {
                p.setNamaPetugasTransient("Belum Ditugaskan");
            }
        }
        return list;
    }

    // Melengkapi nama penjual, produk, dan petugas pada setiap objek PenukaranPoin
    public List<PenukaranPoin> enrichPenukaranList(List<PenukaranPoin> list) {
        Map<Long, String> mapPenjual = penjualRepo.findAll().stream()
                .collect(Collectors.toMap(Penjual::getIdPenjual, Penjual::getNamaPenjual, (a, b) -> a));
        Map<Long, String> mapProduk = produkRepo.findAll().stream()
                .collect(Collectors.toMap(ProdukOlahan::getIdProduk, ProdukOlahan::getNamaProduk, (a, b) -> a));
        Map<Long, String> mapPetugas = petugasRepo.findAll().stream()
                .collect(Collectors.toMap(Petugas::getIdPetugas, Petugas::getNamaPetugas, (a, b) -> a));

        for (PenukaranPoin r : list) {
            r.setNamaPenjualTransient(mapPenjual.getOrDefault(r.getPenjualId(), "Penjual #" + r.getPenjualId()));
            List<DetailPenukaranPoin> details = detailPenukaranRepo.findByPenukaranPoinIdPenukaran(r.getIdPenukaran());
            if (details != null && !details.isEmpty()) {
                String joined = details.stream()
                        .map(d -> d.getJumlahItem() + "x " + d.getProdukOlahan().getNamaProduk())
                        .collect(Collectors.joining(", "));
                r.setNamaProdukTransient(joined);
            } else {
                r.setNamaProdukTransient(mapProduk.getOrDefault(r.getProdukId(), "Produk #" + r.getProdukId()));
            }
            if (r.getPetugasId() != null) {
                r.setNamaPetugasTransient(mapPetugas.getOrDefault(r.getPetugasId(), "Petugas #" + r.getPetugasId()));
            } else {
                r.setNamaPetugasTransient("Belum Ditugaskan");
            }
        }
        return list;
    }

    
    // Mencari transaksi penjualan berdasarkan Nomor Resi DENGAN validasi kepemilikan Penjual
    public Optional<Penjualan> cariBerdasarkanResiUntukPenjual(String kodeResi, Long penjualId) {
        Optional<Penjualan> opt = cariBerdasarkanResi(kodeResi);
        if (opt.isPresent() && penjualId != null && !penjualId.equals(opt.get().getPenjualId())) {
            throw new SecurityException("Akses ditolak! Nomor resi " + kodeResi.toUpperCase() + " bukan milik akun Anda.");
        }
        return opt;
    }

    // Mencari transaksi penukaran poin berdasarkan Nomor Resi DENGAN validasi kepemilikan Penjual
    public Optional<PenukaranPoin> cariRedeemBerdasarkanResiUntukPenjual(String kodeResi, Long penjualId) {
        Optional<PenukaranPoin> opt = cariRedeemBerdasarkanResi(kodeResi);
        if (opt.isPresent() && penjualId != null && !penjualId.equals(opt.get().getPenjualId())) {
            throw new SecurityException("Akses ditolak! Nomor resi " + kodeResi.toUpperCase() + " bukan milik akun Anda.");
        }
        return opt;
    }

    // Mengambil hanya transaksi penjualan yang ditugaskan kepada petugas tertentu
    public List<Penjualan> getTransaksiUntukPetugas(Long petugasId) {
        if (petugasId == null) return List.of();
        return enrichPenjualanList(penjualanRepo.findByPetugasIdOrderByIdTransaksiDesc(petugasId));
    }

    // Mencari transaksi penjualan berdasarkan Nomor Resi (misal: JLT-000001)
    public Optional<Penjualan> cariBerdasarkanResi(String kodeResi) {
        if (kodeResi == null || kodeResi.isBlank()) return Optional.empty();
        String upper = kodeResi.trim().toUpperCase();
        if (upper.startsWith("RDM-")) return Optional.empty();
        String digits = upper.replace("JLT-", "").replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return Optional.empty();
        try {
            Long id = Long.parseLong(digits);
            Optional<Penjualan> opt = penjualanRepo.findById(id);
            opt.ifPresent(p -> enrichPenjualanList(List.of(p)));
            return opt;
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    // Mencari transaksi penukaran poin berdasarkan Nomor Resi (misal: RDM-000001)
    public Optional<PenukaranPoin> cariRedeemBerdasarkanResi(String kodeResi) {
        if (kodeResi == null || kodeResi.isBlank()) return Optional.empty();
        String upper = kodeResi.trim().toUpperCase();
        if (upper.startsWith("JLT-")) return Optional.empty();
        String digits = upper.replace("RDM-", "").replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return Optional.empty();
        try {
            Long id = Long.parseLong(digits);
            Optional<PenukaranPoin> opt = penukaranRepo.findById(id);
            opt.ifPresent(r -> enrichPenukaranList(List.of(r)));
            return opt;
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    // Membuat pesanan pickup baru oleh Penjual (Minimal 15 Liter + Pilihan Alamat & Rekening/E-Wallet)
    @Transactional
    public Penjualan buatPesananPickup(Long penjualId, Double jumlahLiter, String lokasi, String catatan,
                                       String metodePembayaran, String namaPenyedia, String noRekeningTujuan) {
        if (jumlahLiter == null || jumlahLiter < 15.0) {
            throw new IllegalArgumentException("Minimal transaksi penyetoran minyak jelantah adalah 15 Liter!");
        }
        StokMinyak stok = getStokTerbaru();
        if (stok.getJumlahUpdate() >= 0.8 * stok.getBatasStok()) {
            throw new IllegalStateException("Transaksi ditolak! Kapasitas gudang sudah mencapai batas peringatan 80% (" + stok.getJumlahUpdate() + " L / " + stok.getBatasStok() + " L).");
        }
        if ((stok.getJumlahUpdate() + jumlahLiter) > stok.getBatasStok()) {
            throw new IllegalStateException("Transaksi ditolak! Jumlah minyak (" + jumlahLiter + " L) akan membuat gudang melebihi kapasitas maksimal 1000 L.");
        }
        Penjualan trx = new Penjualan();
        trx.setPenjualId(penjualId);
        trx.setJumlahMinyak(jumlahLiter);
        trx.setLokasiPenjemputan(lokasi);
        trx.setCatatan(catatan);
        trx.setMetodePembayaran((metodePembayaran != null && !metodePembayaran.isBlank()) ? metodePembayaran : "Bank");
        trx.setNamaPenyedia((namaPenyedia != null && !namaPenyedia.isBlank()) ? namaPenyedia : "BCA");
        trx.setNoRekeningTujuan(noRekeningTujuan);
        trx.setHargaMinyak(hargaMinyakBerlaku);
        trx.setTotalHarga(jumlahLiter * hargaMinyakBerlaku);
        trx.setHasilPoint(hitungPoinKelipatan15(jumlahLiter));
        trx.setStatus("Transaksi Masuk");
        trx.setKeteranganBukti("Pesanan dibuat oleh Penjual. Menunggu penugasan kurir dari Pusat.");
        trx.setTanggalTransaksi(LocalDate.now());
        trx.setPusatId(1L);
        Penjualan saved = penjualanRepo.save(trx);
        Penjual pPembuat = getPenjualAktif(penjualId);
        catatRiwayatUpdate("JUAL_MINYAK", saved.getIdTransaksi(), null, "Transaksi Masuk", "PENJUAL",
                pPembuat != null ? pPembuat.getNamaPenjual() : "Penjual",
                "Pesanan dibuat oleh Penjual (" + jumlahLiter.intValue() + " L) di " + lokasi + ". Menunggu penugasan kurir dari Pusat.", null);
        buatNotifikasi("Pesanan Pickup " + saved.getKodeResi() + " Dibuat",
                "Permintaan penjemputan " + jumlahLiter.intValue() + " Liter minyak jelantah di " + lokasi + " telah masuk.",
                "TRANSAKSI_MASUK", penjualId, null, 1L);
        return saved;
    }

    // Menugaskan petugas oleh Pusat untuk Jual Minyak
    @Transactional
    public void tugaskanPetugasPickup(Long idTransaksi, Long idPetugas, String pesanUntukPetugas) {
        Penjualan trx = penjualanRepo.findById(idTransaksi)
                .orElseThrow(() -> new IllegalArgumentException("Data transaksi tidak ditemukan."));
        if (!"Transaksi Masuk".equalsIgnoreCase(trx.getStatus())) {
            throw new IllegalStateException("Penugasan kurir hanya dapat dibuat untuk transaksi yang masih berstatus 'Transaksi Masuk'.");
        }

        Petugas ptg = petugasRepo.findById(idPetugas)
                .orElseThrow(() -> new IllegalArgumentException("Data petugas tidak ditemukan."));
        if (!"tersedia".equalsIgnoreCase(ptg.getStatus())) {
            throw new IllegalStateException("Petugas sedang tidak tersedia dan tidak bisa ditugaskan.");
        }
        if ((ptg.getBebanAngkut() + trx.getJumlahMinyak()) > ptg.getKapasitasMaksimal()) {
            throw new IllegalStateException("Kapasitas petugas tidak mencukupi! Beban saat ini: " + ptg.getBebanAngkut() + " L, Tambahan: " + trx.getJumlahMinyak() + " L (Maks: 200 L).");
        }
        if ("REDEEM".equalsIgnoreCase(ptg.getTipeAktivitas())) {
            throw new IllegalStateException("Petugas sedang ditugaskan untuk pengantaran reward. Tidak bisa ditugaskan untuk penjualan minyak sekaligus.");
        }

        String pesanFinal = (pesanUntukPetugas != null && !pesanUntukPetugas.isBlank())
                ? pesanUntukPetugas : "Segera lakukan penjemputan sesuai lokasi penjual.";

        Penugasan tgs = penugasanRepo.findByPenjualanId(idTransaksi).orElse(new Penugasan());
        tgs.setPenjualanId(idTransaksi);
        tgs.setPetugasId(idPetugas);
        tgs.setPusatId(1L);
        tgs.setLokasiPenjual(trx.getLokasiPenjemputan());
        tgs.setPesanPenugasan(pesanFinal);
        tgs.setStatusPenugasan("Penugasan Kurir");
        penugasanRepo.save(tgs);

        Notifikasi notif = new Notifikasi();
        notif.setJudul("Penugasan Pickup " + trx.getKodeResi());
        notif.setPesan(pesanFinal + " (Lokasi: " + trx.getLokasiPenjemputan() + " • " + trx.getJumlahMinyak() + " L)");
        notif.setTipe("PENUGASAN");
        notif.setPetugasId(idPetugas);
        notif.setPenjualId(trx.getPenjualId());
        notif.setPusatId(1L);
        notifikasiRepo.save(notif);

        trx.setPetugasId(idPetugas);
        trx.setStatus("Penugasan Kurir");
        trx.setKeteranganBukti("Kurir " + ptg.getNamaPetugas() + " telah ditugaskan. Pesan Pusat: " + pesanFinal);
        penjualanRepo.save(trx);
        catatRiwayatUpdate("JUAL_MINYAK", trx.getIdTransaksi(), null, "Penugasan Kurir", "PUSAT",
                getPusatAktif().getNamaGudang(),
                "Kurir " + ptg.getNamaPetugas() + " telah ditugaskan. Pesan Pusat: " + pesanFinal, null);

        ptg.setBebanAngkut(ptg.getBebanAngkut() + trx.getJumlahMinyak());
        ptg.setTipeAktivitas("PENJUALAN");
        if (ptg.getBebanAngkut() >= ptg.getKapasitasMaksimal()) {
            ptg.setStatus("tidak tersedia");
        }
        petugasRepo.save(ptg);
    }

    // Update status transaksi jual minyak oleh Petugas atau Pusat
    // Aturan RBAC: Petugas hanya memiliki akses hingga validasi minyak ('Diverifikasi Petugas') atau 'Dibatalkan'.
    @Transactional
    public void verifikasiDanUpdateTransaksiDenganBukti(Long idTransaksi, Double literAktual, String statusBaru,
                                                        String keteranganBukti, MultipartFile fotoBukti, String actorRole) {
        Penjualan trx = penjualanRepo.findById(idTransaksi)
                .orElseThrow(() -> new IllegalArgumentException("Transaksi tidak ditemukan."));
        String statusLama = trx.getStatus();

        if ("PETUGAS".equalsIgnoreCase(actorRole)) {
            if ("Selesai".equalsIgnoreCase(statusBaru)) {
                throw new IllegalArgumentException("Petugas hanya memiliki akses update status hingga validasi minyak ('Diverifikasi Petugas') atau 'Dibatalkan'. Penyelesaian akhir dilakukan oleh Pusat.");
            }
        }

        if (literAktual != null && literAktual >= 15.0) {
            trx.setJumlahMinyak(literAktual);
            trx.setTotalHarga(literAktual * trx.getHargaMinyak());
            trx.setHasilPoint(hitungPoinKelipatan15(literAktual));
        }

        String fotoBase64 = convertFileToBase64(fotoBukti);
        if (fotoBase64 != null) {
            trx.setFoto(fotoBase64);
        }
        if (keteranganBukti != null && !keteranganBukti.isBlank()) {
            trx.setKeteranganBukti("[" + actorRole + " - " + statusBaru + "]: " + keteranganBukti);
        }

        trx.setStatus(statusBaru);

        // Jika transaksi dibatalkan, kurangi beban petugas bila sebelumnya telah ditugaskan
        if ("Dibatalkan".equalsIgnoreCase(statusBaru) && !"Dibatalkan".equalsIgnoreCase(statusLama)) {
            if (trx.getPetugasId() != null) {
                Petugas ptg = petugasRepo.findById(trx.getPetugasId()).orElse(null);
                if (ptg != null) {
                    ptg.setBebanAngkut(Math.max(0.0, ptg.getBebanAngkut() - trx.getJumlahMinyak()));
                    if (ptg.getBebanAngkut() < ptg.getKapasitasMaksimal()) {
                        ptg.setStatus("tersedia");
                    }
                    petugasRepo.save(ptg);
                }
            }
        }

        // Jika status berubah menjadi Selesai oleh Pusat, tambahkan poin ke penjual
        if ("Selesai".equalsIgnoreCase(statusBaru) && !"Selesai".equalsIgnoreCase(statusLama)) {
            Penjual penjual = penjualRepo.findById(trx.getPenjualId()).orElse(null);
            if (penjual != null) {
                int tambahanPoin = trx.getHasilPoint() != null ? trx.getHasilPoint().intValue() : 0;
                penjual.setPoin(penjual.getPoin() + tambahanPoin);
                penjualRepo.save(penjual);
            }
            // Trigger: reset beban angkut petugas saat transaksi selesai
            if (trx.getPetugasId() != null) {
                Petugas ptgSelesai = petugasRepo.findById(trx.getPetugasId()).orElse(null);
                if (ptgSelesai != null) {
                    ptgSelesai.setBebanAngkut(Math.max(0.0, ptgSelesai.getBebanAngkut() - trx.getJumlahMinyak()));
                    // Cek apakah masih ada tugas penjualan aktif untuk petugas ini
                    long tugasAktif = penjualanRepo.findByPetugasIdOrderByIdTransaksiDesc(ptgSelesai.getIdPetugas())
                        .stream().filter(tj -> !"Selesai".equalsIgnoreCase(tj.getStatus()) && !"Dibatalkan".equalsIgnoreCase(tj.getStatus())).count();
                    if (tugasAktif == 0) {
                        ptgSelesai.setTipeAktivitas(null);
                    }
                    if (ptgSelesai.getBebanAngkut() < ptgSelesai.getKapasitasMaksimal()) {
                        ptgSelesai.setStatus("tersedia");
                    }
                    petugasRepo.save(ptgSelesai);
                }
            }
        }

        penjualanRepo.save(trx);
        String namaAktorTrx = "PUSAT".equalsIgnoreCase(actorRole)
                ? getPusatAktif().getNamaGudang()
                : (trx.getPetugasId() != null ? getPetugasAktif(trx.getPetugasId()).getNamaPetugas() : "Petugas");
        catatRiwayatUpdate("JUAL_MINYAK", trx.getIdTransaksi(), null, statusBaru, actorRole, namaAktorTrx,
                (keteranganBukti != null && !keteranganBukti.isBlank()) ? keteranganBukti : "Update status menjadi " + statusBaru,
                fotoBase64);
        buatNotifikasi("Update Status " + trx.getKodeResi() + ": " + statusBaru,
                "Status transaksi minyak diperbarui oleh " + actorRole + " menjadi '" + statusBaru + "'. Bukti: " + (keteranganBukti != null ? keteranganBukti : "-"),
                "UPDATE_TRANSAKSI", trx.getPenjualId(), trx.getPetugasId(), 1L);
    }

    @Transactional
    public void kirimStokTangkiPusat() {
        StokMinyak reset = new StokMinyak();
        reset.setJumlahMinyak(0.0);
        reset.setJumlahUpdate(0.0);
        reset.setBatasStok(1000.0);
        reset.setPusatId(1L);
        reset.setTglUpdate(LocalDate.now());
        stokRepo.save(reset);
    }

    // =====================================================================
    // FLOW REDEEM POINT 5 TAHAPAN:
    // 1. Request Masuk -> 2. Redeem Dikonfirmasi -> 3. Petugas Ditugaskan -> 4. Petugas Dalam Perjalanan -> 5. Selesai
    // =====================================================================

    // 1. Penjual melakukan request redeem poin
    @Transactional
    public PenukaranPoin requestRedeemPoinMulti(Long penjualId, List<Long> produkIdList, List<Integer> qtyList, String alamatPengantaran, String catatan) {
        if (produkIdList == null || produkIdList.isEmpty()) {
            throw new IllegalArgumentException("Pilih minimal satu produk olahan untuk ditukar!");
        }
        Penjual penjual = getPenjualAktif(penjualId);
        int totalPoinDibutuhkan = 0;
        List<String> rincianProduk = new ArrayList<>();

        for (int i = 0; i < produkIdList.size(); i++) {
            Long pId = produkIdList.get(i);
            int q = (qtyList != null && i < qtyList.size() && qtyList.get(i) != null) ? qtyList.get(i) : 1;
            ProdukOlahan produk = produkRepo.findById(pId)
                    .orElseThrow(() -> new IllegalArgumentException("Produk ID " + pId + " tidak ditemukan."));
            if (produk.getStok() < q) {
                throw new IllegalStateException("Stok produk '" + produk.getNamaProduk() + "' tidak mencukupi (Tersisa: " + produk.getStok() + ").");
            }
            totalPoinDibutuhkan += (produk.getHargaPoin().intValue() * q);
            rincianProduk.add(q + "x " + produk.getNamaProduk());
        }

        if (penjual.getPoin() < totalPoinDibutuhkan) {
            throw new IllegalStateException("Saldo poin Anda (" + penjual.getPoin() + " Poin) tidak mencukupi untuk total penukaran (" + totalPoinDibutuhkan + " Poin).");
        }

        penjual.setPoin(penjual.getPoin() - totalPoinDibutuhkan);
        penjualRepo.save(penjual);

        Long produkUtamaId = produkIdList.get(0);
        int qtyUtama = qtyList != null && !qtyList.isEmpty() ? qtyList.get(0) : 1;
        ProdukOlahan produkUtama = produkRepo.findById(produkUtamaId).get();

        for (int i = 0; i < produkIdList.size(); i++) {
            Long pId = produkIdList.get(i);
            int q = (qtyList != null && i < qtyList.size() && qtyList.get(i) != null) ? qtyList.get(i) : 1;
            ProdukOlahan p = produkRepo.findById(pId).get();
            p.setStok(p.getStok() - q);
            produkRepo.save(p);
        }

        PenukaranPoin rdm = new PenukaranPoin();
        rdm.setPenjualId(penjual.getIdPenjual());
        rdm.setProdukId(produkUtamaId);
        rdm.setJumlahUnit(qtyUtama);
        rdm.setHargaPoinSatuan(produkUtama.getHargaPoin().intValue());
        rdm.setJumlahPoin((double) totalPoinDibutuhkan);
        rdm.setJumlahPoinSisa((double) penjual.getPoin());
        rdm.setAlamatPengantaran(alamatPengantaran);
        rdm.setCatatan((catatan != null ? catatan : "") + " [Multi-Item: " + String.join(", ", rincianProduk) + "]");
        rdm.setStatus("Request Masuk");
        rdm.setKeteranganBukti("Permintaan redeem multi-produk dibuat oleh Penjual. Menunggu konfirmasi dari Pusat.");
        rdm.setPusatId(1L);
        PenukaranPoin savedRdm = penukaranRepo.save(rdm);

        for (int i = 0; i < produkIdList.size(); i++) {
            Long pId = produkIdList.get(i);
            int q = (qtyList != null && i < qtyList.size() && qtyList.get(i) != null) ? qtyList.get(i) : 1;
            ProdukOlahan p = produkRepo.findById(pId).get();
            DetailPenukaranPoin det = new DetailPenukaranPoin();
            det.setPenukaranPoin(savedRdm);
            det.setProdukOlahan(p);
            det.setJumlahItem(q);
            det.setHargaPoinSatuan(p.getHargaPoin().intValue());
            det.setSubtotalPoin(p.getHargaPoin().intValue() * q);
            detailPenukaranRepo.save(det);
        }

        catatRiwayatUpdate("REDEEM", null, savedRdm.getIdPenukaran(), "Request Masuk", "PENJUAL",
                penjual.getNamaPenjual(),
                "Permintaan redeem multi-produk: " + String.join(", ", rincianProduk) + " (" + totalPoinDibutuhkan + " Poin).", null);
        buatNotifikasi("Request Redeem Multi-Item " + savedRdm.getKodeResi() + " Masuk",
                "Permintaan tukar poin untuk " + String.join(", ", rincianProduk) + " (" + totalPoinDibutuhkan + " Poin) menunggu konfirmasi Pusat.",
                "REDEEM_MASUK", penjual.getIdPenjual(), null, 1L);
        return savedRdm;
    }
    
    public PenukaranPoin requestRedeemPoin(Long penjualId, Long produkId, int qty, String alamatPengantaran, String catatan) {
        Penjual penjual = getPenjualAktif(penjualId);
        ProdukOlahan produk = produkRepo.findById(produkId)
                .orElseThrow(() -> new IllegalArgumentException("Produk tidak ditemukan."));

        int jumlahUnit = Math.max(1, qty);
        int hargaSatuan = produk.getHargaPoin().intValue();
        int totalPoinDibutuhkan = hargaSatuan * jumlahUnit;

        if (produk.getStok() < jumlahUnit) {
            throw new IllegalStateException("Stok produk '" + produk.getNamaProduk() + "' tidak mencukupi (Tersisa: " + produk.getStok() + ").");
        }
        if (penjual.getPoin() < totalPoinDibutuhkan) {
            throw new IllegalStateException("Saldo poin Anda (" + penjual.getPoin() + " Poin) tidak mencukupi untuk menukar " + jumlahUnit + "x " + produk.getNamaProduk() + " (" + totalPoinDibutuhkan + " Poin).");
        }

        penjual.setPoin(penjual.getPoin() - totalPoinDibutuhkan);
        penjualRepo.save(penjual);

        produk.setStok(produk.getStok() - jumlahUnit);
        produkRepo.save(produk);

        PenukaranPoin rdm = new PenukaranPoin();
        rdm.setPenjualId(penjual.getIdPenjual());
        rdm.setProdukId(produk.getIdProduk());
        rdm.setJumlahUnit(jumlahUnit);
        rdm.setHargaPoinSatuan(hargaSatuan);
        rdm.setJumlahPoin((double) totalPoinDibutuhkan);
        rdm.setJumlahPoinSisa((double) penjual.getPoin());
        rdm.setAlamatPengantaran(alamatPengantaran);
        rdm.setCatatan(catatan);
        rdm.setStatus("Request Masuk");
        rdm.setKeteranganBukti("Permintaan redeem poin dibuat oleh Penjual. Menunggu konfirmasi dari Pusat.");
        rdm.setPusatId(1L);
        PenukaranPoin savedRdm = penukaranRepo.save(rdm);
        buatNotifikasi("Request Redeem " + savedRdm.getKodeResi() + " Masuk",
                "Permintaan tukar poin " + jumlahUnit + "x " + produk.getNamaProduk() + " (" + totalPoinDibutuhkan + " Poin) menunggu konfirmasi Pusat.",
                "REDEEM_MASUK", penjual.getIdPenjual(), null, 1L);
        return savedRdm;
    }

    // 2. Pusat mengonfirmasi redeem poin (Tanpa perlu upload foto bukti, cukup catatan konfirmasi)
    @Transactional
    public void konfirmasiRedeemOlehPusat(Long idPenukaran, String keteranganBukti) {
        PenukaranPoin rdm = penukaranRepo.findById(idPenukaran)
                .orElseThrow(() -> new IllegalArgumentException("Data penukaran tidak ditemukan."));
        if (!"Request Masuk".equalsIgnoreCase(rdm.getStatus())) {
            throw new IllegalStateException("Konfirmasi hanya dapat dilakukan untuk permintaan berstatus 'Request Masuk'.");
        }

        String ketFinal = (keteranganBukti != null && !keteranganBukti.isBlank())
                ? keteranganBukti : "Permintaan redeem telah dikonfirmasi Pusat dan siap ditugaskan ke kurir.";
        rdm.setKeteranganBukti("[PUSAT - Redeem Dikonfirmasi]: " + ketFinal);
        rdm.setStatus("Redeem Dikonfirmasi");
        penukaranRepo.save(rdm);
        catatRiwayatUpdate("REDEEM", null, rdm.getIdPenukaran(), "Redeem Dikonfirmasi", "PUSAT",
                getPusatAktif().getNamaGudang(), ketFinal, null);
        buatNotifikasi("Redeem " + rdm.getKodeResi() + " Dikonfirmasi Pusat",
                "Permintaan penukaran poin Anda telah dikonfirmasi oleh Pusat dan segera ditugaskan ke kurir pengantaran.",
                "REDEEM_KONFIRMASI", rdm.getPenjualId(), null, 1L);
    }

    // 3. Pusat menugaskan petugas kurir untuk mengantar reward ke penjual
    @Transactional
    public void tugaskanPetugasRedeem(Long idPenukaran, Long idPetugas, String pesanUntukPetugas) {
        PenukaranPoin rdm = penukaranRepo.findById(idPenukaran)
                .orElseThrow(() -> new IllegalArgumentException("Data penukaran tidak ditemukan."));
        if (!"Redeem Dikonfirmasi".equalsIgnoreCase(rdm.getStatus())) {
            throw new IllegalStateException("Redeem harus dikonfirmasi oleh Pusat terlebih dahulu sebelum menugaskan petugas.");
        }

        Petugas ptg = petugasRepo.findById(idPetugas)
                .orElseThrow(() -> new IllegalArgumentException("Petugas tidak ditemukan."));
        if (!"tersedia".equalsIgnoreCase(ptg.getStatus())) {
            throw new IllegalStateException("Petugas sedang tidak tersedia.");
        }
        if ("PENJUALAN".equalsIgnoreCase(ptg.getTipeAktivitas())) {
            throw new IllegalStateException("Petugas sedang ditugaskan untuk penjualan minyak. Tidak bisa ditugaskan untuk pengantaran reward sekaligus.");
        }

        String pesanFinal = (pesanUntukPetugas != null && !pesanUntukPetugas.isBlank())
                ? pesanUntukPetugas : "Antarkan produk reward ke alamat penjual.";

        rdm.setPetugasId(idPetugas);
        rdm.setStatus("Petugas Ditugaskan");
        rdm.setKeteranganBukti("[PUSAT]: Petugas " + ptg.getNamaPetugas() + " ditugaskan. Pesan: " + pesanFinal);
        penukaranRepo.save(rdm);
        catatRiwayatUpdate("REDEEM", null, rdm.getIdPenukaran(), "Petugas Ditugaskan", "PUSAT",
                getPusatAktif().getNamaGudang(),
                "Petugas " + ptg.getNamaPetugas() + " ditugaskan untuk pengantaran reward. Pesan: " + pesanFinal, null);

        Notifikasi notif = new Notifikasi();
        notif.setJudul("Pengantaran Reward " + rdm.getKodeResi());
        notif.setPesan(pesanFinal + " (Tujuan: " + rdm.getAlamatPengantaran() + ")");
        notif.setTipe("PENUGASAN_REDEEM");
        notif.setPetugasId(idPetugas);
        notif.setPenjualId(rdm.getPenjualId());
        notif.setPusatId(1L);
        notifikasiRepo.save(notif);

        ptg.setTipeAktivitas("REDEEM");
        ptg.setStatus("tidak tersedia");
        petugasRepo.save(ptg);
    }

    // 4 & 5. Petugas atau Pusat mengupdate status redeem (Petugas Dalam Perjalanan, Selesai, atau Dibatalkan) dengan Bukti Foto
    @Transactional
    public void verifikasiDanUpdateRedeemDenganBukti(Long idPenukaran, String statusBaru, String keteranganBukti,
                                                     MultipartFile fotoBukti, String actorRole) {
        PenukaranPoin rdm = penukaranRepo.findById(idPenukaran)
                .orElseThrow(() -> new IllegalArgumentException("Data penukaran tidak ditemukan."));
        String statusLama = rdm.getStatus();

        if ("Dibatalkan".equalsIgnoreCase(statusBaru) && !"Dibatalkan".equalsIgnoreCase(statusLama)) {
            Penjual penjual = penjualRepo.findById(rdm.getPenjualId()).orElse(null);
            if (penjual != null) {
                penjual.setPoin(penjual.getPoin() + rdm.getJumlahPoin().intValue());
                penjualRepo.save(penjual);
            }
            List<DetailPenukaranPoin> details = detailPenukaranRepo.findByPenukaranPoinIdPenukaran(rdm.getIdPenukaran());
            if (details != null && !details.isEmpty()) {
                for (DetailPenukaranPoin det : details) {
                    ProdukOlahan pr = det.getProdukOlahan();
                    if (pr != null) {
                        pr.setStok(pr.getStok() + det.getJumlahItem());
                        produkRepo.save(pr);
                    }
                }
            } else if (rdm.getProdukId() != null) {
                ProdukOlahan produk = produkRepo.findById(rdm.getProdukId()).orElse(null);
                if (produk != null) {
                    int qty = rdm.getJumlahUnit() != null ? rdm.getJumlahUnit() : 1;
                    produk.setStok(produk.getStok() + qty);
                    produkRepo.save(produk);
                }
            }
        }

        String fotoBase64 = convertFileToBase64(fotoBukti);
        if (fotoBase64 != null) {
            rdm.setFoto(fotoBase64);
        }
        if (keteranganBukti != null && !keteranganBukti.isBlank()) {
            rdm.setKeteranganBukti("[" + actorRole + " - " + statusBaru + "]: " + keteranganBukti);
        }

        rdm.setStatus(statusBaru);
        penukaranRepo.save(rdm);
        String namaAktorRdm = "PUSAT".equalsIgnoreCase(actorRole)
                ? getPusatAktif().getNamaGudang()
                : (rdm.getPetugasId() != null ? getPetugasAktif(rdm.getPetugasId()).getNamaPetugas() : "Petugas");
        catatRiwayatUpdate("REDEEM", null, rdm.getIdPenukaran(), statusBaru, actorRole, namaAktorRdm,
                (keteranganBukti != null && !keteranganBukti.isBlank()) ? keteranganBukti : "Update status menjadi " + statusBaru,
                fotoBase64);
        buatNotifikasi("Update Pengantaran Reward " + rdm.getKodeResi() + ": " + statusBaru,
                "Status penukaran poin diperbarui oleh " + actorRole + " menjadi '" + statusBaru + "'. Catatan: " + (keteranganBukti != null ? keteranganBukti : "-"),
                "UPDATE_REDEEM", rdm.getPenjualId(), rdm.getPetugasId(), 1L);

        // Trigger: reset tipeAktivitas petugas saat redeem selesai atau dibatalkan
        if (("Selesai".equalsIgnoreCase(statusBaru) || "Dibatalkan".equalsIgnoreCase(statusBaru))
                && rdm.getPetugasId() != null) {
            Petugas ptgRdm = petugasRepo.findById(rdm.getPetugasId()).orElse(null);
            if (ptgRdm != null) {
                // Cek apakah masih ada tugas redeem aktif untuk petugas ini
                long redeemAktif = penukaranRepo.findByPetugasIdOrderByIdPenukaranDesc(ptgRdm.getIdPetugas())
                    .stream().filter(r -> !"Selesai".equalsIgnoreCase(r.getStatus()) && !"Dibatalkan".equalsIgnoreCase(r.getStatus())).count();
                if (redeemAktif <= 1) { // <= 1 karena yang sedang diproses belum tersimpan
                    ptgRdm.setTipeAktivitas(null);
                    ptgRdm.setStatus("tersedia");
                    petugasRepo.save(ptgRdm);
                }
            }
        }
    }

    @Transactional
    public void batalkanPenukaranPoin(Long idPenukaran) {
        verifikasiDanUpdateRedeemDenganBukti(idPenukaran, "Dibatalkan", "Penukaran dibatalkan oleh Pusat. Poin dan stok dikembalikan.", null, "PUSAT");
    }

    // Menggabungkan seluruh transaksi jual minyak dan redeem poin dalam satu list riwayat terpadu
    public List<TransaksiRiwayatDTO> getRiwayatUnifiedForPenjual(Long penjualId) {
        List<Penjualan> listPenjualan = (penjualId != null)
                ? penjualanRepo.findAll().stream().filter(p -> penjualId.equals(p.getPenjualId())).toList()
                : penjualanRepo.findAll();
        enrichPenjualanList(listPenjualan);

        List<PenukaranPoin> listRedeem = (penjualId != null)
                ? penukaranRepo.findByPenjualIdOrderByIdPenukaranDesc(penjualId)
                : penukaranRepo.findAll();
        enrichPenukaranList(listRedeem);

        List<TransaksiRiwayatDTO> unified = new ArrayList<>();

        for (Penjualan p : listPenjualan) {
            TransaksiRiwayatDTO dto = new TransaksiRiwayatDTO();
            dto.setJenis("JUAL_MINYAK");
            dto.setJenisLabel("Jual Minyak");
            dto.setId(p.getIdTransaksi());
            dto.setKodeResi(p.getKodeResi());
            dto.setTanggal(p.getTanggalTransaksi());
            dto.setNamaPenjual(p.getNamaPenjualTransient());
            dto.setNamaPetugas(p.getNamaPetugasTransient());
            dto.setStatus(p.getStatus());
            dto.setDetailItem(p.getJumlahMinyak() + " Liter Minyak Jelantah");
            dto.setTotalBayar(p.getTotalHarga());
            dto.setPoin(p.getHasilPoint());
            dto.setAlamat(p.getLokasiPenjemputan());
            dto.setCatatan(p.getCatatan());
            dto.setKeteranganBukti(p.getKeteranganBukti());
            dto.setFoto(p.getFoto());
            dto.setLiter(p.getJumlahMinyak());
            dto.setHargaSatuan(p.getHargaMinyak());
            unified.add(dto);
        }

        for (PenukaranPoin r : listRedeem) {
            TransaksiRiwayatDTO dto = new TransaksiRiwayatDTO();
            dto.setJenis("REDEEM");
            dto.setJenisLabel("Redeem Poin");
            dto.setId(r.getIdPenukaran());
            dto.setKodeResi(r.getKodeResi());
            dto.setTanggal(r.getTglPenukaran());
            dto.setNamaPenjual(r.getNamaPenjualTransient());
            dto.setNamaPetugas(r.getNamaPetugasTransient());
            dto.setStatus(r.getStatus());
            int unit = r.getJumlahUnit() != null ? r.getJumlahUnit() : 1;
            dto.setDetailItem(unit + "x " + r.getNamaProdukTransient());
            dto.setTotalBayar(null);
            dto.setPoin(-r.getJumlahPoin());
            dto.setAlamat(r.getAlamatPengantaran());
            dto.setCatatan(r.getCatatan());
            dto.setKeteranganBukti(r.getKeteranganBukti());
            dto.setFoto(r.getFoto());
            dto.setLiter(null);
            dto.setHargaSatuan(r.getHargaPoinSatuan() != null ? r.getHargaPoinSatuan().doubleValue() : null);
            unified.add(dto);
        }

        unified.sort((a, b) -> {
            int dateCmp = b.getTanggal().compareTo(a.getTanggal());
            if (dateCmp != 0) return dateCmp;
            return b.getId().compareTo(a.getId());
        });

        return unified;
    }

    // Menyimpan produk olahan dengan dukungan upload file gambar
    @Transactional
    public void simpanProdukOlahan(Long idProduk, String nama, String deskripsi, Double hargaPoin, Integer stok, MultipartFile gambarFile) {
        ProdukOlahan p = (idProduk != null) ? produkRepo.findById(idProduk).orElse(new ProdukOlahan()) : new ProdukOlahan();
        p.setNamaProduk(nama);
        p.setDeskripsi(deskripsi);
        p.setHargaPoin(hargaPoin);
        p.setStok(stok);
        String base64Img = convertFileToBase64(gambarFile);
        if (base64Img != null) {
            p.setGambar(base64Img);
        } else if (p.getGambar() == null || p.getGambar().isBlank()) {
            p.setGambar("https://placehold.co/300x200/FFF3B8/926008?text=" + nama.replace(" ", "+"));
        }
        p.setPusatId(1L);
        produkRepo.save(p);
    }

    @Transactional
    public void hapusProdukOlahan(Long idProduk) {
        produkRepo.deleteById(idProduk);
    }


    // Membuat record notifikasi baru untuk Penjual, Petugas, dan/atau Pusat
    @Transactional
    public void buatNotifikasi(String judul, String pesan, String tipe, Long penjualId, Long petugasId, Long pusatId) {
        Notifikasi n = new Notifikasi();
        n.setJudul(judul);
        n.setPesan(pesan);
        n.setTipe(tipe);
        n.setPenjualId(penjualId);
        n.setPetugasId(petugasId);
        n.setPusatId(pusatId);
        n.setStatusBaca(false);
        notifikasiRepo.save(n);
    }

    public List<Notifikasi> getNotifikasiPenjual(Long penjualId) {
        if (penjualId == null) return List.of();
        return notifikasiRepo.findByPenjualIdOrderByIdNotifikasiDesc(penjualId);
    }

    public List<Notifikasi> getNotifikasiPusat() {
        return notifikasiRepo.findByPusatIdOrderByIdNotifikasiDesc(1L);
    }

    public long getUnreadNotifCount(String role, Long userId) {
        if ("PUSAT".equalsIgnoreCase(role)) {
            return notifikasiRepo.countByPusatIdAndStatusBacaFalse(1L);
        } else if ("PETUGAS".equalsIgnoreCase(role) && userId != null) {
            return notifikasiRepo.countByPetugasIdAndStatusBacaFalse(userId);
        } else if ("PENJUAL".equalsIgnoreCase(role) && userId != null) {
            return notifikasiRepo.countByPenjualIdAndStatusBacaFalse(userId);
        }
        return 0;
    }

    @Transactional
    public void tandaiSemuaNotifikasiDibaca(String role, Long userId) {
        List<Notifikasi> list = List.of();
        if ("PUSAT".equalsIgnoreCase(role)) {
            list = notifikasiRepo.findByPusatIdOrderByIdNotifikasiDesc(1L);
        } else if ("PETUGAS".equalsIgnoreCase(role) && userId != null) {
            list = notifikasiRepo.findByPetugasIdOrderByIdNotifikasiDesc(userId);
        } else if ("PENJUAL".equalsIgnoreCase(role) && userId != null) {
            list = notifikasiRepo.findByPenjualIdOrderByIdNotifikasiDesc(userId);
        }
        for (Notifikasi n : list) {
            if (!Boolean.TRUE.equals(n.getStatusBaca())) {
                n.setStatusBaca(true);
                notifikasiRepo.save(n);
            }
        }
    }

    public List<ProdukOlahan> getSemuaProduk() { return produkRepo.findAllByOrderByIdProdukAsc(); }
    public List<Penjualan> getSemuaTransaksi() { return enrichPenjualanList(penjualanRepo.findAllByOrderByIdTransaksiDesc()); }
    public List<Petugas> getSemuaPetugas() { return petugasRepo.findAll(); }
    public List<Petugas> getPetugasTersedia() { return petugasRepo.findByStatusIgnoreCase("tersedia"); }
    public List<Notifikasi> getNotifikasiPetugas(Long petugasId) { return notifikasiRepo.findByPetugasIdOrderByIdNotifikasiDesc(petugasId); }

    public List<PenukaranPoin> getSemuaPenukaran() {
        return enrichPenukaranList(penukaranRepo.findAllByOrderByIdPenukaranDesc());
    }

    public List<PenukaranPoin> getPenukaranUntukPetugas(Long petugasId) {
        if (petugasId != null) {
            return enrichPenukaranList(penukaranRepo.findByPetugasIdOrderByIdPenukaranDesc(petugasId));
        }
        return enrichPenukaranList(penukaranRepo.findAllByOrderByIdPenukaranDesc());
    }

    // Simpan atau Update akun Petugas beserta Pendaftaran Role & Pemberian Hak Akses oleh Pusat
    @Transactional
    public Petugas simpanAtauUpdatePetugas(Long idPetugas, String namaPetugas, String email, String password,
                                           String noTelp, String alamat, Double kapasitasMaksimal,
                                           String role, String statusAkses) {
        Petugas ptg = (idPetugas != null)
                ? petugasRepo.findById(idPetugas).orElse(new Petugas())
                : new Petugas();

        ptg.setNamaPetugas(namaPetugas);
        ptg.setEmail(email.trim());
        if (password != null && !password.isBlank()) {
            ptg.setPassword(password);
        } else if (ptg.getPassword() == null || ptg.getPassword().isBlank()) {
            ptg.setPassword("petugas123");
        }
        ptg.setNoTelp(noTelp.trim());
        ptg.setAlamat(alamat);
        ptg.setKapasitasMaksimal(kapasitasMaksimal != null && kapasitasMaksimal > 0 ? kapasitasMaksimal : 200.0);
        ptg.setRole((role != null && !role.isBlank()) ? role.toUpperCase() : "PETUGAS");

        if (idPetugas == null) {
            ptg.setBebanAngkut(0.0);
        }
        if (statusAkses != null && !statusAkses.isBlank()) {
            ptg.setStatus(statusAkses.toLowerCase());
        } else if (ptg.getStatus() == null) {
            ptg.setStatus("tersedia");
        }
        return petugasRepo.save(ptg);
    }

    @Transactional
    public void hapusPetugas(Long idPetugas) {
        petugasRepo.deleteById(idPetugas);
    }

    // Reset beban angkut petugas oleh Pusat (manual reset)
    @Transactional
    public void resetBebanPetugas(Long idPetugas) {
        Petugas ptg = petugasRepo.findById(idPetugas)
            .orElseThrow(() -> new IllegalArgumentException("Petugas tidak ditemukan."));
        ptg.setBebanAngkut(0.0);
        ptg.setTipeAktivitas(null);
        ptg.setStatus("tersedia");
        petugasRepo.save(ptg);
    }

    // Data grafik penjualan 7 hari terakhir untuk overview pusat
    public List<Map<String, Object>> getDataGrafikPenjualan7Hari() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate tgl = LocalDate.now().minusDays(i);
            long count = penjualanRepo.countByTanggalTransaksi(tgl);
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("tanggal", tgl.toString());
            row.put("label", tgl.getDayOfWeek().toString().substring(0, 3));
            row.put("jumlah", count);
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> getDataGrafikRedeem7Hari() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate tgl = LocalDate.now().minusDays(i);
            long count = penukaranRepo.countByTglPenukaran(tgl);
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("tanggal", tgl.toString());
            row.put("label", tgl.getDayOfWeek().toString().substring(0, 3));
            row.put("jumlah", count);
            result.add(row);
        }
        return result;
    }


    // Data grafik mingguan (7 hari terakhir) khusus untuk Petugas: berapa kali angkut & berapa liter minyak dibawa
    public List<Map<String, Object>> getDataGrafikPetugas7Hari(Long petugasId) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate tgl = LocalDate.now().minusDays(i);
            long kaliAngkut = (petugasId != null) ? penjualanRepo.countByPetugasIdAndTanggalTransaksi(petugasId, tgl) : 0;
            Double literAngkut = (petugasId != null) ? penjualanRepo.sumJumlahMinyakByPetugasAndTanggal(petugasId, tgl) : 0.0;
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("tanggal", tgl.toString());
            row.put("label", tgl.getDayOfWeek().toString().substring(0, 3) + " (" + tgl.getDayOfMonth() + "/" + tgl.getMonthValue() + ")");
            row.put("kaliAngkut", kaliAngkut);
            row.put("literAngkut", literAngkut != null ? literAngkut : 0.0);
            result.add(row);
        }
        return result;
    }

    // Update Profil Penjual (termasuk Metode Pembayaran Bank/E-Wallet, Penyedia, & Nomor Rekening/E-Wallet)
    @Transactional
    public void updateProfilPenjual(Long penjualId, String namaPenjual, String email, String noHp,
                                    String kategoriPenjual, String alamat, String metodePembayaran,
                                    String namaPenyedia, String noRekening, String passwordBaru, MultipartFile fileFoto) {
        Penjual p = getPenjualAktif(penjualId);
        if (namaPenjual != null && !namaPenjual.isBlank()) p.setNamaPenjual(namaPenjual.trim());
        if (email != null && !email.isBlank()) p.setEmail(email.trim());
        if (noHp != null && !noHp.isBlank()) p.setNoHp(noHp.trim());
        if (kategoriPenjual != null && !kategoriPenjual.isBlank()) p.setKategoriPenjual(kategoriPenjual);
        if (alamat != null && !alamat.isBlank()) p.setAlamat(alamat.trim());
        if (metodePembayaran != null && !metodePembayaran.isBlank()) p.setMetodePembayaran(metodePembayaran);
        if (namaPenyedia != null && !namaPenyedia.isBlank()) p.setNamaPenyedia(namaPenyedia);
        if (noRekening != null) p.setNoRekening(noRekening.trim());
        if (passwordBaru != null && !passwordBaru.isBlank()) p.setPassword(passwordEncoder.encodePassword(passwordBaru));
        String base64Foto = convertFileToBase64(fileFoto);
        if (base64Foto != null) p.setFotoProfil(base64Foto);
        penjualRepo.save(p);
    }

    // Update Profil Petugas
    @Transactional
    public void updateProfilPetugas(Long petugasId, String namaPetugas, String email, String noTelp,
                                    String alamat, String passwordBaru) {
        Petugas ptg = getPetugasAktif(petugasId);
        if (namaPetugas != null && !namaPetugas.isBlank()) ptg.setNamaPetugas(namaPetugas.trim());
        if (email != null && !email.isBlank()) ptg.setEmail(email.trim());
        if (noTelp != null && !noTelp.isBlank()) ptg.setNoTelp(noTelp.trim());
        if (alamat != null && !alamat.isBlank()) ptg.setAlamat(alamat.trim());
        if (passwordBaru != null && !passwordBaru.isBlank()) ptg.setPassword(passwordEncoder.encodePassword(passwordBaru));
        petugasRepo.save(ptg);
    }

    // Update Profil Pusat
    @Transactional
    public void updateProfilPusat(String namaGudang, String namaAdmin, String email, String noTelepon,
                                  String alamat, String passwordBaru) {
        Pusat pst = getPusatAktif();
        if (namaGudang != null && !namaGudang.isBlank()) pst.setNamaGudang(namaGudang.trim());
        if (namaAdmin != null && !namaAdmin.isBlank()) pst.setNama(namaAdmin.trim());
        if (email != null && !email.isBlank()) pst.setEmail(email.trim());
        if (noTelepon != null && !noTelepon.isBlank()) pst.setNoTelepon(noTelepon.trim());
        if (alamat != null && !alamat.isBlank()) pst.setAlamat(alamat.trim());
        if (passwordBaru != null && !passwordBaru.isBlank()) pst.setPassword(passwordEncoder.encodePassword(passwordBaru));
        pusatRepo.save(pst);
    }

    @Transactional
    public void lengkapiProfilPenjual(Long penjualId, String kategoriPenjual, String alamat) {
        Penjual penjual = penjualRepo.findById(penjualId)
            .orElseThrow(() -> new IllegalArgumentException("Penjual tidak ditemukan."));
        if (kategoriPenjual != null && !kategoriPenjual.isBlank()) {
            penjual.setKategoriPenjual(kategoriPenjual);
        }
        if (alamat != null && !alamat.isBlank()) {
            penjual.setAlamat(alamat);
        }
        penjualRepo.save(penjual);
    }

    // =========================================================================
    // APPEND-ONLY RIWAYAT UPDATE TRANSAKSI (TIDAK MENIMPA LOG SEBELUMNYA)
    // =========================================================================
    @Transactional
    public void catatRiwayatUpdate(String jenisTransaksi, Long penjualanId, Long penukaranId,
                                   String status, String aktorRole, String namaAktor,
                                   String keteranganBukti, String fotoBukti) {
        RiwayatUpdateTransaksi log = new RiwayatUpdateTransaksi();
        log.setJenisTransaksi(jenisTransaksi);
        log.setPenjualanId(penjualanId);
        log.setPenukaranId(penukaranId);
        log.setStatus(status);
        log.setAktorRole(aktorRole);
        log.setNamaAktor(namaAktor);
        log.setKeteranganBukti(keteranganBukti);
        log.setFotoBukti(fotoBukti);
        log.setWaktuUpdate(LocalDateTime.now());
        riwayatUpdateRepo.save(log);
    }

    public List<RiwayatUpdateTransaksi> getRiwayatUpdatePenjualan(Long penjualanId) {
        if (penjualanId == null) return List.of();
        return riwayatUpdateRepo.findByPenjualanIdOrderByWaktuUpdateAscIdRiwayatAsc(penjualanId);
    }

    public List<RiwayatUpdateTransaksi> getRiwayatUpdatePenukaran(Long penukaranId) {
        if (penukaranId == null) return List.of();
        return riwayatUpdateRepo.findByPenukaranIdOrderByWaktuUpdateAscIdRiwayatAsc(penukaranId);
    }

    public Optional<Penjualan> getPenjualanByIdEnriched(Long idTransaksi) {
        if (idTransaksi == null) return Optional.empty();
        Optional<Penjualan> opt = penjualanRepo.findById(idTransaksi);
        opt.ifPresent(p -> enrichPenjualanList(List.of(p)));
        return opt;
    }

    public Optional<PenukaranPoin> getPenukaranByIdEnriched(Long idPenukaran) {
        if (idPenukaran == null) return Optional.empty();
        Optional<PenukaranPoin> opt = penukaranRepo.findById(idPenukaran);
        opt.ifPresent(r -> enrichPenukaranList(List.of(r)));
        return opt;
    }

    // =========================================================================
    // RIWAYAT PENUGASAN TERPADU UNTUK POV PETUGAS (JUAL MINYAK + REDEEM POIN)
    // =========================================================================
    public List<TransaksiRiwayatDTO> getRiwayatPenugasanUnifiedForPetugas(Long petugasId) {
        if (petugasId == null) return List.of();
        List<Penjualan> listPenjualan = enrichPenjualanList(penjualanRepo.findByPetugasIdOrderByIdTransaksiDesc(petugasId));
        List<PenukaranPoin> listRedeem = enrichPenukaranList(penukaranRepo.findByPetugasIdOrderByIdPenukaranDesc(petugasId));

        List<TransaksiRiwayatDTO> unified = new ArrayList<>();

        for (Penjualan p : listPenjualan) {
            TransaksiRiwayatDTO dto = new TransaksiRiwayatDTO();
            dto.setJenis("JUAL_MINYAK");
            dto.setJenisLabel("Jual Minyak");
            dto.setId(p.getIdTransaksi());
            dto.setKodeResi(p.getKodeResi());
            dto.setTanggal(p.getTanggalTransaksi());
            dto.setNamaPenjual(p.getNamaPenjualTransient());
            dto.setNamaPetugas(p.getNamaPetugasTransient());
            dto.setStatus(p.getStatus());
            dto.setDetailItem(p.getJumlahMinyak() + " Liter Minyak Jelantah");
            dto.setTotalBayar(p.getTotalHarga());
            dto.setPoin(p.getHasilPoint());
            dto.setAlamat(p.getLokasiPenjemputan());
            dto.setCatatan(p.getCatatan());
            dto.setKeteranganBukti(p.getKeteranganBukti());
            dto.setFoto(p.getFoto());
            dto.setLiter(p.getJumlahMinyak());
            dto.setHargaSatuan(p.getHargaMinyak());
            unified.add(dto);
        }

        for (PenukaranPoin r : listRedeem) {
            TransaksiRiwayatDTO dto = new TransaksiRiwayatDTO();
            dto.setJenis("REDEEM");
            dto.setJenisLabel("Redeem Poin");
            dto.setId(r.getIdPenukaran());
            dto.setKodeResi(r.getKodeResi());
            dto.setTanggal(r.getTglPenukaran());
            dto.setNamaPenjual(r.getNamaPenjualTransient());
            dto.setNamaPetugas(r.getNamaPetugasTransient());
            dto.setStatus(r.getStatus());
            dto.setDetailItem(r.getNamaProdukTransient());
            dto.setTotalBayar(null);
            dto.setPoin(-r.getJumlahPoin());
            dto.setAlamat(r.getAlamatPengantaran());
            dto.setCatatan(r.getCatatan());
            dto.setKeteranganBukti(r.getKeteranganBukti());
            dto.setFoto(r.getFoto());
            dto.setLiter(null);
            dto.setHargaSatuan(r.getHargaPoinSatuan() != null ? r.getHargaPoinSatuan().doubleValue() : null);
            unified.add(dto);
        }

        unified.sort((a, b) -> {
            int dateCmp = b.getTanggal().compareTo(a.getTanggal());
            if (dateCmp != 0) return dateCmp;
            return b.getId().compareTo(a.getId());
        });
        return unified;
    }

    // =========================================================================
    // AUTENTIKASI / AUTO-REGISTER AKUN GOOGLE (OAUTH2 / GOOGLE IDENTITY)
    // =========================================================================
    public record GoogleAuthResult(AuthSession session, boolean perluLengkapiProfil) {}

    @Transactional
    public GoogleAuthResult authenticateOrRegisterGoogle(String email, String nama, String googleId, String pictureUrl) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email akun Google tidak valid.");
        }
        String cleanEmail = email.trim().toLowerCase();

        // 1. Cek apakah email terdaftar sebagai PUSAT
        Optional<Pusat> optPusat = pusatRepo.findByEmail(cleanEmail);
        if (optPusat.isPresent()) {
            Pusat p = optPusat.get();
            return new GoogleAuthResult(new AuthSession(p.getIdPusat(), p.getNamaGudang(), p.getEmail(), "PUSAT"), false);
        }

        // 2. Cek apakah email terdaftar sebagai PETUGAS aktif
        Optional<Petugas> optPetugas = petugasRepo.findByEmail(cleanEmail);
        if (optPetugas.isPresent()) {
            Petugas pt = optPetugas.get();
            if ("NONAKTIF".equalsIgnoreCase(pt.getRole())) {
                throw new IllegalStateException("Akses akun petugas Anda sedang dinonaktifkan oleh Pusat.");
            }
            return new GoogleAuthResult(new AuthSession(pt.getIdPetugas(), pt.getNamaPetugas(), pt.getEmail(), "PETUGAS"), false);
        }

        // 3. Cek apakah sudah terdaftar sebagai PENJUAL (berdasarkan googleId atau email)
        Optional<Penjual> optPenjual = (googleId != null && !googleId.isBlank())
                ? penjualRepo.findByGoogleId(googleId)
                : Optional.empty();
        if (optPenjual.isEmpty()) {
            optPenjual = penjualRepo.findByEmail(cleanEmail);
        }

        if (optPenjual.isPresent()) {
            Penjual pj = optPenjual.get();
            if (googleId != null && !googleId.isBlank() && (pj.getGoogleId() == null || pj.getGoogleId().isBlank())) {
                pj.setGoogleId(googleId);
                pj.setAuthProvider("GOOGLE");
            }
            if (pictureUrl != null && !pictureUrl.isBlank() && (pj.getFotoProfil() == null || pj.getFotoProfil().isBlank())) {
                pj.setFotoProfil(pictureUrl);
            }
            penjualRepo.save(pj);
            boolean belumLengkap = (pj.getAlamat() == null || pj.getAlamat().isBlank() || pj.getNoRekening() == null || pj.getNoRekening().isBlank());
            return new GoogleAuthResult(new AuthSession(pj.getIdPenjual(), pj.getNamaPenjual(), pj.getEmail(), "PENJUAL"), belumLengkap);
        }

        // 4. Auto-register PENJUAL baru menggunakan akun Google
        Penjual baru = new Penjual();
        baru.setNamaPenjual((nama != null && !nama.isBlank()) ? nama.trim() : cleanEmail.split("@")[0]);
        baru.setEmail(cleanEmail);
        baru.setPassword(passwordEncoder.generateGooglePlaceholderPassword());
        baru.setGoogleId(googleId);
        baru.setAuthProvider("GOOGLE");
        baru.setFotoProfil(pictureUrl);
        baru.setPoin(0);
        baru.setRole("PENJUAL");
        Penjual tersimpan = penjualRepo.save(baru);

        buatNotifikasi("Selamat Bergabung via Google!",
                "Akun penjual " + tersimpan.getNamaPenjual() + " berhasil didaftarkan menggunakan akun Google. Silakan lengkapi profil Anda.",
                "REGISTRASI", tersimpan.getIdPenjual(), null, 1L);

        return new GoogleAuthResult(new AuthSession(tersimpan.getIdPenjual(), tersimpan.getNamaPenjual(), tersimpan.getEmail(), "PENJUAL"), true);
    }

}
