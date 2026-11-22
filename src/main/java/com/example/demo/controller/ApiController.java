package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.service.JelantaraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// =========================================================================
// KONSEP OOP PADA CONTROLLER INI:
// 1. SEPARATION OF CONCERNS: Memisahkan seluruh endpoint REST JSON API
//    (/api/**) dari controller MVC Thymeleaf agar dapat diuji langsung
//    melalui Postman, curl, atau integrasi frontend/mobile.
// 2. ENCAPSULATION: Helper mapping (toPetugasDto, toPenukaranDto) bersifat
//    private untuk menyembunyikan data sensitif (password) dan mencegah
//    rekursi JSON pada relasi JPA.
// =========================================================================
@RestController
@RequestMapping("/api")
public class ApiController {

    private final JelantaraService service;

    public ApiController(JelantaraService service) {
        this.service = service;
    }

    // 1. Overview & Monitoring Gudang Pusat
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        StokMinyak stok = service.getStokTerbaru();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("hargaMinyakPerLiter", service.getHargaMinyakBerlaku());
        res.put("stokTerisiLiter", stok != null ? stok.getJumlahUpdate() : 0.0);
        res.put("kapasitasMaksimalLiter", stok != null ? stok.getBatasStok() : 1000.0);
        res.put("persentaseKapasitas", stok != null ? stok.getPersentaseKapasitas() : 0.0);
        res.put("transaksiHariIni", service.getPermintaanTransaksiHariIni());
        res.put("redeemHariIni", service.getPermintaanRedeemHariIni());
        res.put("minyakMasukHariIni", service.getBanyakMinyakMasukHariIni());
        res.put("petugasTersedia", service.getTotalPetugasAvail());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stok")
    public ResponseEntity<StokMinyak> getStokTerbaru() {
        return ResponseEntity.ok(service.getStokTerbaru());
    }

    @GetMapping("/harga-minyak")
    public ResponseEntity<Map<String, Object>> getHargaMinyak() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("hargaPerLiter", service.getHargaMinyakBerlaku());
        res.put("mataUang", "IDR");
        return ResponseEntity.ok(res);
    }

    // 2. Katalog Produk Olahan (GET, POST, DELETE)
    @GetMapping("/produk")
    public ResponseEntity<List<ProdukOlahan>> getSemuaProduk() {
        return ResponseEntity.ok(service.getSemuaProduk());
    }

    @PostMapping("/produk")
    public ResponseEntity<Map<String, Object>> simpanProduk(@RequestBody Map<String, Object> body) {
        try {
            Long idProduk = body.get("idProduk") != null ? Long.valueOf(body.get("idProduk").toString()) : null;
            String nama = body.get("namaProduk") != null ? body.get("namaProduk").toString() : "";
            String deskripsi = body.get("deskripsi") != null ? body.get("deskripsi").toString() : "";
            Double hargaPoin = body.get("hargaPoin") != null ? Double.valueOf(body.get("hargaPoin").toString()) : 0.0;
            Integer stok = body.get("stok") != null ? Integer.valueOf(body.get("stok").toString()) : 0;

            service.simpanProdukOlahan(idProduk, nama, deskripsi, hargaPoin, stok, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "success",
                    "message", "Produk olahan berhasil disimpan."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/produk/{id}")
    public ResponseEntity<Map<String, Object>> hapusProduk(@PathVariable Long id) {
        try {
            service.hapusProdukOlahan(id);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Produk ID " + id + " berhasil dihapus."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // 3. Transaksi Penjualan Minyak (GET, Lacak Resi, POST)
    @GetMapping("/transaksi")
    public ResponseEntity<List<Penjualan>> getSemuaTransaksi() {
        return ResponseEntity.ok(service.getSemuaTransaksi());
    }

    @GetMapping("/transaksi/resi/{kodeResi}")
    public ResponseEntity<?> lacakTransaksiByResi(@PathVariable String kodeResi) {
        return service.cariBerdasarkanResi(kodeResi)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Transaksi dengan resi " + kodeResi + " tidak ditemukan.")));
    }

    @PostMapping("/transaksi")
    public ResponseEntity<Map<String, Object>> buatTransaksiPickup(@RequestBody Map<String, Object> body) {
        try {
            Long penjualId = Long.valueOf(body.get("penjualId").toString());
            Double jumlahLiter = Double.valueOf(body.get("jumlahLiter").toString());
            String lokasi = body.get("lokasi").toString();
            String catatan = body.get("catatan") != null ? body.get("catatan").toString() : "";
            String metodePembayaran = body.get("metodePembayaran") != null ? body.get("metodePembayaran").toString() : "Bank";
            String namaPenyedia = body.get("namaPenyedia") != null ? body.get("namaPenyedia").toString() : "BCA";
            String noRekeningTujuan = body.get("noRekeningTujuan") != null ? body.get("noRekeningTujuan").toString() : "";

            Penjualan baru = service.buatPesananPickup(
                    penjualId, jumlahLiter, lokasi, catatan, metodePembayaran, namaPenyedia, noRekeningTujuan
            );
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("status", "success");
            res.put("message", "Pesanan pickup berhasil dibuat.");
            res.put("kodeResi", baru.getKodeResi());
            res.put("data", baru);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // 4. Transaksi Redeem Poin Multi-Produk (GET, Lacak Resi, POST)
    @GetMapping("/redeem")
    public ResponseEntity<List<Map<String, Object>>> getSemuaRedeem() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PenukaranPoin p : service.getSemuaPenukaran()) {
            result.add(toPenukaranDto(p));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/redeem/resi/{kodeResi}")
    public ResponseEntity<?> lacakRedeemByResi(@PathVariable String kodeResi) {
        return service.cariRedeemBerdasarkanResi(kodeResi)
                .<ResponseEntity<?>>map(p -> ResponseEntity.ok(toPenukaranDto(p)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("status", "error", "message", "Redeem dengan resi " + kodeResi + " tidak ditemukan.")));
    }

    @PostMapping("/redeem")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> buatRedeemMulti(@RequestBody Map<String, Object> body) {
        try {
            Long penjualId = Long.valueOf(body.get("penjualId").toString());
            List<?> rawProdukIds = (List<?>) body.get("produkIdList");
            List<?> rawQtys = (List<?>) body.get("qtyList");
            List<Long> produkIdList = new ArrayList<>();
            List<Integer> qtyList = new ArrayList<>();
            if (rawProdukIds != null) {
                for (Object o : rawProdukIds) produkIdList.add(Long.valueOf(o.toString()));
            }
            if (rawQtys != null) {
                for (Object o : rawQtys) qtyList.add(Integer.valueOf(o.toString()));
            }
            String alamat = body.get("alamatPengantaran").toString();
            String catatan = body.get("catatan") != null ? body.get("catatan").toString() : "";

            PenukaranPoin rdm = service.requestRedeemPoinMulti(penjualId, produkIdList, qtyList, alamat, catatan);
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("status", "success");
            res.put("message", "Permintaan redeem berhasil dibuat.");
            res.put("kodeResi", rdm.getKodeResi());
            res.put("data", toPenukaranDto(rdm));
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // 5. Petugas Kurir (Tanpa mengekspos password)
    @GetMapping("/petugas")
    public ResponseEntity<List<Map<String, Object>>> getSemuaPetugas() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Petugas pt : service.getSemuaPetugas()) {
            result.add(toPetugasDto(pt));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/petugas/tersedia")
    public ResponseEntity<List<Map<String, Object>>> getPetugasTersedia() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Petugas pt : service.getPetugasTersedia()) {
            result.add(toPetugasDto(pt));
        }
        return ResponseEntity.ok(result);
    }

    // 6. Uji Autentikasi Login (Verifikasi Email & Password Terenkripsi)
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> apiLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Email dan password wajib diisi."));
        }
        Optional<JelantaraService.AuthSession> opt = service.authenticateUser(email, password);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Email atau password salah."));
        }
        JelantaraService.AuthSession user = opt.get();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "success");
        res.put("id", user.id());
        res.put("nama", user.nama());
        res.put("email", user.email());
        res.put("role", user.role());
        return ResponseEntity.ok(res);
    }

    // --- Helper Mapping Private (Aman dari rekursi JPA & kebocoran password) ---
    private Map<String, Object> toPetugasDto(Petugas pt) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idPetugas", pt.getIdPetugas());
        map.put("namaPetugas", pt.getNamaPetugas());
        map.put("email", pt.getEmail());
        map.put("noTelp", pt.getNoTelp());
        map.put("status", pt.getStatus());
        map.put("role", pt.getRole());
        map.put("bebanAngkut", pt.getBebanAngkut());
        map.put("kapasitasMaksimal", pt.getKapasitasMaksimal());
        map.put("alamat", pt.getAlamat());
        return map;
    }

    private Map<String, Object> toPenukaranDto(PenukaranPoin p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("idPenukaran", p.getIdPenukaran());
        map.put("kodeResi", p.getKodeResi());
        map.put("tglPenukaran", p.getTglPenukaran());
        map.put("status", p.getStatus());
        map.put("jumlahPoin", p.getJumlahPoin());
        map.put("jumlahPoinSisa", p.getJumlahPoinSisa());
        map.put("alamatPengantaran", p.getAlamatPengantaran());
        map.put("catatan", p.getCatatan());
        map.put("penjualId", p.getPenjualId());
        map.put("petugasId", p.getPetugasId());
        map.put("namaPenjual", p.getNamaPenjualTransient());
        map.put("rincianProduk", p.getNamaProdukTransient());
        return map;
    }
}
