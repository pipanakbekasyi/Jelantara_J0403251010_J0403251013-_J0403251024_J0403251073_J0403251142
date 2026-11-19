package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.JelantaraService;

// =========================================================================
// KONSEP OOP PADA CONTROLLER INI:
// 1. ENCAPSULATION & ABSTRACTION: Mengisolasi operasi Role PETUGAS dan
//    menyembunyikan pengisian atribut model berulang melalui method private
//    loadPetugasContext().
// =========================================================================
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller Modular khusus untuk Role PETUGAS (Tugas Transaksi + Grafik Mingguan, Tugas Redeem, Notifikasi, & Halaman Profile)
@Controller
public class PetugasController {

    private final JelantaraService service;

    public PetugasController(JelantaraService service) {
        this.service = service;
    }

    private boolean isPetugas(HttpSession session) {
        return "PETUGAS".equalsIgnoreCase((String) session.getAttribute("userRole"));
    }

    private void loadPetugasContext(HttpSession session, Model model, String activeMenu) {
        Long idPetugas = (Long) session.getAttribute("userId");
        Petugas ptg = service.getPetugasAktif(idPetugas);
        if ("notifikasi".equalsIgnoreCase(activeMenu)) {
            service.tandaiSemuaNotifikasiDibaca("PETUGAS", ptg.getIdPetugas());
        }
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("petugas", ptg);
        model.addAttribute("stok", service.getStokTerbaru());
        model.addAttribute("transaksiList", service.getTransaksiUntukPetugas(ptg.getIdPetugas()));
        model.addAttribute("penukaranList", service.getPenukaranUntukPetugas(ptg.getIdPetugas()));
        model.addAttribute("pesanList", service.getNotifikasiPetugas(ptg.getIdPetugas()));
        model.addAttribute("grafikMingguanPetugas", service.getDataGrafikPetugas7Hari(ptg.getIdPetugas()));
        model.addAttribute("unreadNotif", service.getUnreadNotifCount("PETUGAS", ptg.getIdPetugas()));
    }

    @GetMapping({"/petugas", "/petugas/transaksi"})
    public String halamanTugasTransaksi(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPetugas(session)) {
            ra.addFlashAttribute("flashError", "Akses ditolak! Halaman tersebut khusus untuk Role PETUGAS.");
            return "redirect:/login";
        }
        loadPetugasContext(session, model, "transaksi");
        return "petugas/index";
    }

    @GetMapping("/petugas/redeem")
    public String halamanTugasRedeem(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPetugas(session)) {
            ra.addFlashAttribute("flashError", "Akses ditolak! Halaman tersebut khusus untuk Role PETUGAS.");
            return "redirect:/login";
        }
        loadPetugasContext(session, model, "redeem");
        return "petugas/redeem";
    }

    
    @GetMapping("/petugas/riwayat")
    public String halamanRiwayatPenugasanPetugas(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPetugas(session)) {
            ra.addFlashAttribute("flashError", "Akses ditolak! Halaman tersebut khusus untuk Role PETUGAS.");
            return "redirect:/login";
        }
        loadPetugasContext(session, model, "riwayat");
        Long idPetugas = (Long) session.getAttribute("userId");
        Petugas ptg = service.getPetugasAktif(idPetugas);
        model.addAttribute("riwayatPenugasanList", service.getRiwayatPenugasanUnifiedForPetugas(ptg.getIdPetugas()));
        return "petugas/riwayat";
    }

    @GetMapping("/petugas/notifikasi")
    public String halamanNotifikasiPetugas(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPetugas(session)) {
            ra.addFlashAttribute("flashError", "Akses ditolak! Halaman tersebut khusus untuk Role PETUGAS.");
            return "redirect:/login";
        }
        loadPetugasContext(session, model, "notifikasi");
        return "petugas/notifikasi";
    }

    @GetMapping("/petugas/profile")
    public String halamanProfilPetugas(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPetugas(session)) {
            ra.addFlashAttribute("flashError", "Akses ditolak! Halaman tersebut khusus untuk Role PETUGAS.");
            return "redirect:/login";
        }
        loadPetugasContext(session, model, "profile");
        return "petugas/profile";
    }

    @PostMapping("/petugas/profile/update")
    public String updateProfilPetugas(@RequestParam String namaPetugas,
                                      @RequestParam String email,
                                      @RequestParam String noTelp,
                                      @RequestParam String alamat,
                                      @RequestParam(required = false) String passwordBaru,
                                      HttpSession session, RedirectAttributes ra) {
        if (!isPetugas(session)) return "redirect:/login";
        try {
            Long idPetugas = (Long) session.getAttribute("userId");
            service.updateProfilPetugas(idPetugas, namaPetugas, email, noTelp, alamat, passwordBaru);
            session.setAttribute("userName", namaPetugas);
            ra.addFlashAttribute("flashSuccess", "Profil petugas berhasil diperbarui!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal memperbarui profil: " + e.getMessage());
        }
        return "redirect:/petugas/profile";
    }

    @PostMapping("/petugas/verifikasi")
    public String verifikasiOlehPetugas(@RequestParam Long idTransaksi, @RequestParam Double jumlahMinyakAktual,
                                        @RequestParam String status, @RequestParam String keteranganBukti,
                                        @RequestParam(value = "fotoBukti", required = false) MultipartFile fotoBukti,
                                        HttpSession session, RedirectAttributes ra) {
        if (!isPetugas(session)) return "redirect:/login";
        try {
            service.verifikasiDanUpdateTransaksiDenganBukti(idTransaksi, jumlahMinyakAktual, status, keteranganBukti, fotoBukti, "PETUGAS");
            ra.addFlashAttribute("flashSuccess", "Transaksi JLT-" + String.format("%06d", idTransaksi) + " berhasil diperbarui menjadi '" + status + "' beserta bukti foto!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Update gagal: " + e.getMessage());
        }
        return "redirect:/petugas/transaksi";
    }

    @PostMapping("/petugas/redeem/verifikasi")
    public String verifikasiRedeemOlehPetugas(@RequestParam Long idPenukaran,
                                              @RequestParam String status,
                                              @RequestParam String keteranganBukti,
                                              @RequestParam(value = "fotoBukti", required = false) MultipartFile fotoBukti,
                                              HttpSession session, RedirectAttributes ra) {
        if (!isPetugas(session)) return "redirect:/login";
        try {
            service.verifikasiDanUpdateRedeemDenganBukti(idPenukaran, status, keteranganBukti, fotoBukti, "PETUGAS");
            ra.addFlashAttribute("flashSuccess", "Status pengantaran reward RDM-" + String.format("%06d", idPenukaran) + " berhasil diperbarui menjadi '" + status + "'!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Update gagal: " + e.getMessage());
        }
        return "redirect:/petugas/redeem";
    }
}
