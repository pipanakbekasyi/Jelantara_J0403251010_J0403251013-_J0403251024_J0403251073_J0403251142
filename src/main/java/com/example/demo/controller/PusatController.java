package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.JelantaraService;

// =========================================================================
// KONSEP OOP PADA CONTROLLER INI:
// 1. ENCAPSULATION & MODULARITY: Mengisolasi seluruh hak akses kontrol penuh
//    Role PUSAT dengan helper validasi akses private isPusat(HttpSession).
// =========================================================================
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller Modular khusus untuk Role PUSAT (Overview, Transaksi, Katalog Produk, Verifikasi Redeem, Kelola Petugas, Notifikasi)
@Controller
public class PusatController {

    private final JelantaraService service;

    public PusatController(JelantaraService service) {
        this.service = service;
    }

    private boolean isPusat(HttpSession session) {
        return "PUSAT".equalsIgnoreCase((String) session.getAttribute("userRole"));
    }

    @GetMapping("/pusat")
    public String dashboardPusat(@RequestParam(defaultValue = "overview") String menu,
                                 HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPusat(session)) {
            ra.addFlashAttribute("flashError", "Akses ditolak! Halaman tersebut hanya untuk Role PUSAT.");
            return "redirect:/login";
        }
        if ("notifikasi".equalsIgnoreCase(menu)) {
            service.tandaiSemuaNotifikasiDibaca("PUSAT", 1L);
        }

        StokMinyak stok = service.getStokTerbaru();
        model.addAttribute("activeMenu", menu);
        model.addAttribute("pusat", service.getPusatAktif());
        model.addAttribute("stok", stok);
        model.addAttribute("hargaAcuan", service.getHargaMinyakBerlaku());

        model.addAttribute("redeemHariIni", service.getPermintaanRedeemHariIni());
        model.addAttribute("transaksiHariIni", service.getPermintaanTransaksiHariIni());
        model.addAttribute("petugasAvailCount", service.getTotalPetugasAvail());
        model.addAttribute("minyakMasukHariIni", service.getBanyakMinyakMasukHariIni());

        model.addAttribute("transaksiList", service.getSemuaTransaksi());
        model.addAttribute("produkList", service.getSemuaProduk());
        model.addAttribute("petugasList", service.getSemuaPetugas());
        model.addAttribute("petugasTersediaList", service.getPetugasTersedia());
        model.addAttribute("penukaranList", service.getSemuaPenukaran());
        model.addAttribute("notifikasiList", service.getNotifikasiPusat());
        model.addAttribute("unreadNotif", service.getUnreadNotifCount("PUSAT", 1L));
        model.addAttribute("grafikPenjualan", service.getDataGrafikPenjualan7Hari());
        model.addAttribute("grafikRedeem", service.getDataGrafikRedeem7Hari());

        if (stok.getPersentaseKapasitas() >= 80.0) {
            model.addAttribute("flashWarning", "Peringatan Kapasitas (FR-03): Tangki gudang mencapai " + stok.getPersentaseKapasitas() + "%! Segera jadwalkan pengiriman.");
        }
        return "pusat/index";
    }

    @PostMapping("/pusat/update-harga")
    public String updateHargaMinyak(@RequestParam Double hargaBaru, HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.updateHargaMinyakOlehPusat(hargaBaru);
            ra.addFlashAttribute("flashSuccess", "Harga acuan minyak berhasil diperbarui menjadi Rp " + String.format("%,.0f", hargaBaru) + " / Liter.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/pusat?menu=overview";
    }

    @PostMapping("/pusat/assign-petugas")
    public String assignPetugas(@RequestParam Long idTransaksi, @RequestParam Long idPetugas,
                                @RequestParam(required = false) String pesanUntukPetugas,
                                HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.tugaskanPetugasPickup(idTransaksi, idPetugas, pesanUntukPetugas);
            ra.addFlashAttribute("flashSuccess", "Petugas berhasil ditugaskan & notifikasi dikirim untuk Resi JLT-" + String.format("%06d", idTransaksi));
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/pusat?menu=transaksi";
    }

    @PostMapping("/pusat/update-transaksi")
    public String editTransaksiPusat(@RequestParam Long idTransaksi, @RequestParam Double jumlahMinyak,
                                     @RequestParam String status, @RequestParam String keteranganBukti,
                                     @RequestParam(value = "fotoBukti", required = false) MultipartFile fotoBukti,
                                     HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.verifikasiDanUpdateTransaksiDenganBukti(idTransaksi, jumlahMinyak, status, keteranganBukti, fotoBukti, "PUSAT");
            ra.addFlashAttribute("flashSuccess", "Status transaksi JLT-" + String.format("%06d", idTransaksi) + " berhasil diperbarui beserta bukti!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal memperbarui transaksi: " + e.getMessage());
        }
        return "redirect:/pusat?menu=transaksi";
    }

    @PostMapping("/pusat/redeem/konfirmasi")
    public String konfirmasiRedeemPusat(@RequestParam Long idPenukaran,
                                        @RequestParam(required = false) String keteranganBukti,
                                        HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.konfirmasiRedeemOlehPusat(idPenukaran, keteranganBukti);
            ra.addFlashAttribute("flashSuccess", "Redeem RDM-" + String.format("%06d", idPenukaran) + " berhasil dikonfirmasi oleh Pusat. Silakan tugaskan kurir.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/pusat?menu=redeem";
    }

    @PostMapping("/pusat/redeem/assign-petugas")
    public String assignPetugasRedeem(@RequestParam Long idPenukaran, @RequestParam Long idPetugas,
                                      @RequestParam(required = false) String pesanUntukPetugas,
                                      HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.tugaskanPetugasRedeem(idPenukaran, idPetugas, pesanUntukPetugas);
            ra.addFlashAttribute("flashSuccess", "Petugas berhasil ditugaskan untuk pengantaran reward RDM-" + String.format("%06d", idPenukaran));
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/pusat?menu=redeem";
    }

    @PostMapping("/pusat/redeem/update")
    public String updateRedeemPusat(@RequestParam Long idPenukaran, @RequestParam String status,
                                    @RequestParam String keteranganBukti,
                                    @RequestParam(value = "fotoBukti", required = false) MultipartFile fotoBukti,
                                    HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.verifikasiDanUpdateRedeemDenganBukti(idPenukaran, status, keteranganBukti, fotoBukti, "PUSAT");
            ra.addFlashAttribute("flashSuccess", "Status redeem RDM-" + String.format("%06d", idPenukaran) + " berhasil diperbarui menjadi '" + status + "'!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal memperbarui redeem: " + e.getMessage());
        }
        return "redirect:/pusat?menu=redeem";
    }

    @PostMapping("/pusat/reset-tangki")
    public String resetTangkiPusat(HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        service.kirimStokTangkiPusat();
        ra.addFlashAttribute("flashSuccess", "Pengiriman minyak dari tangki pusat berhasil dicatat! Volume tangki direset ke 0 Liter.");
        return "redirect:/pusat?menu=overview";
    }

    @PostMapping("/pusat/produk/save")
    public String saveProduk(@RequestParam(required = false) Long idProduk, @RequestParam String namaProduk,
                             @RequestParam String deskripsi, @RequestParam Double hargaPoin,
                             @RequestParam Integer stok,
                             @RequestParam(value = "gambarFile", required = false) MultipartFile gambarFile,
                             HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.simpanProdukOlahan(idProduk, namaProduk, deskripsi, hargaPoin, stok, gambarFile);
            ra.addFlashAttribute("flashSuccess", "Produk '" + namaProduk + "' berhasil disimpan ke katalog.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal menyimpan produk: " + e.getMessage());
        }
        return "redirect:/pusat?menu=katalog";
    }

    @PostMapping("/pusat/produk/delete")
    public String deleteProduk(@RequestParam Long idProduk, HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.hapusProdukOlahan(idProduk);
            ra.addFlashAttribute("flashSuccess", "Produk olahan berhasil dihapus dari katalog.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal menghapus produk: " + e.getMessage());
        }
        return "redirect:/pusat?menu=katalog";
    }

    @PostMapping("/pusat/penukaran/batal")
    public String batalkanPenukaran(@RequestParam Long idPenukaran, HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.batalkanPenukaranPoin(idPenukaran);
            ra.addFlashAttribute("flashSuccess", "Penukaran dibatalkan. Poin penjual dan stok produk otomatis dikembalikan!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/pusat?menu=redeem";
    }

    @PostMapping({"/pusat/petugas/save", "/pusat/petugas/tambah"})
    public String savePetugas(@RequestParam(required = false) Long idPetugas,
                              @RequestParam String namaPetugas,
                              @RequestParam String email,
                              @RequestParam(required = false) String password,
                              @RequestParam String noTelp,
                              @RequestParam(required = false) String alamat,
                              @RequestParam(required = false) Double kapasitasMaksimal,
                              @RequestParam(defaultValue = "PETUGAS") String role,
                              @RequestParam(defaultValue = "tersedia") String status,
                              HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.simpanAtauUpdatePetugas(idPetugas, namaPetugas, email, password, noTelp, alamat, kapasitasMaksimal, role, status);
            String aksi = (idPetugas != null) ? "diperbarui" : "didaftarkan dengan hak akses " + role;
            ra.addFlashAttribute("flashSuccess", "Data & hak akses petugas '" + namaPetugas + "' berhasil " + aksi + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal menyimpan data petugas: " + e.getMessage());
        }
        return "redirect:/pusat?menu=petugas";
    }

    @PostMapping("/pusat/petugas/hapus")
    public String hapusPetugas(@RequestParam Long idPetugas, HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.hapusPetugas(idPetugas);
            ra.addFlashAttribute("flashSuccess", "Akun petugas berhasil dihapus.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal hapus petugas: " + e.getMessage());
        }
        return "redirect:/pusat?menu=petugas";
    }

    @PostMapping("/pusat/petugas/reset-beban")
    public String resetBebanPetugas(@RequestParam Long idPetugas, HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.resetBebanPetugas(idPetugas);
            ra.addFlashAttribute("flashSuccess", "Beban angkut petugas berhasil direset. Status kembali tersedia.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal reset beban: " + e.getMessage());
        }
        return "redirect:/pusat?menu=petugas";
    }

    @PostMapping("/pusat/profile/update")
    public String updateProfilPusat(@RequestParam String namaGudang,
                                    @RequestParam String nama,
                                    @RequestParam String email,
                                    @RequestParam String noTelepon,
                                    @RequestParam String alamat,
                                    @RequestParam(required = false) String passwordBaru,
                                    HttpSession session, RedirectAttributes ra) {
        if (!isPusat(session)) return "redirect:/login";
        try {
            service.updateProfilPusat(namaGudang, nama, email, noTelepon, alamat, passwordBaru);
            session.setAttribute("userName", namaGudang);
            ra.addFlashAttribute("flashSuccess", "Profil Gudang Pusat berhasil diperbarui!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal memperbarui profil pusat: " + e.getMessage());
        }
        return "redirect:/pusat?menu=profile";
    }
}