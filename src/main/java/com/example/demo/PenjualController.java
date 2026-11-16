package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

// Controller Modular khusus untuk Role PENJUAL (Check It Now, Halaman Profil & Edit Profil, serta Halaman Notifikasi di Navbar)
@Controller
public class PenjualController {

    private final JelantaraService service;

    public PenjualController(JelantaraService service) {
        this.service = service;
    }

    private boolean isPenjual(HttpSession session) {
        return "PENJUAL".equalsIgnoreCase((String) session.getAttribute("userRole"));
    }

    @GetMapping({"/dashboard", "/check-it-now", "/dashboard/order", "/dashboard/tracker"})
    public String checkItNowPenjual(@RequestParam(defaultValue = "order") String tab,
                                    @RequestParam(required = false) String resi,
                                    @RequestParam(required = false) Long produkId,
                                    Model model, HttpSession session, RedirectAttributes ra) {
        String role = (String) session.getAttribute("userRole");
        if (role == null) {
            ra.addFlashAttribute("flashError", "Silakan login terlebih dahulu untuk mengakses modul Check it Now.");
            return "redirect:/login";
        }
        if ("PUSAT".equalsIgnoreCase(role)) return "redirect:/pusat";
        if ("PETUGAS".equalsIgnoreCase(role)) return "redirect:/petugas";

        if ("notifikasi".equalsIgnoreCase(tab)) {
            return "redirect:/penjual/notifikasi";
        }
        if (produkId != null) {
            tab = "redeem";
            model.addAttribute("selectedProdukId", produkId);
        }

        Penjual penjual = service.getPenjualAktif((Long) session.getAttribute("userId"));
        Penjualan hasilCariPenjualan = null;
        PenukaranPoin hasilCariRedeem = null;
        boolean resiTidakDitemukan = false;

        if (resi != null && !resi.isBlank()) {
            tab = "tracker";
            try {
                Optional<Penjualan> foundJlt = service.cariBerdasarkanResiUntukPenjual(resi, penjual.getIdPenjual());
                if (foundJlt.isPresent()) {
                    hasilCariPenjualan = foundJlt.get();
                } else {
                    Optional<PenukaranPoin> foundRdm = service.cariRedeemBerdasarkanResiUntukPenjual(resi, penjual.getIdPenjual());
                    if (foundRdm.isPresent()) {
                        hasilCariRedeem = foundRdm.get();
                    } else {
                        resiTidakDitemukan = true;
                    }
                }
            } catch (SecurityException se) {
                model.addAttribute("resiUnauthorized", se.getMessage());
            }
        }

        model.addAttribute("userRole", role);
        model.addAttribute("penjual", penjual);
        model.addAttribute("activeTab", tab);
        model.addAttribute("searchedResi", resi);
        model.addAttribute("trackedOrder", hasilCariPenjualan);
        model.addAttribute("trackedRedeem", hasilCariRedeem);
        model.addAttribute("resiNotFound", resiTidakDitemukan);
        model.addAttribute("hargaAcuan", service.getHargaMinyakBerlaku());

        model.addAttribute("riwayatUnified", service.getRiwayatUnifiedForPenjual(penjual.getIdPenjual()));
        model.addAttribute("unreadNotif", service.getUnreadNotifCount("PENJUAL", penjual.getIdPenjual()));
        model.addAttribute("produkList", service.getSemuaProduk());
        model.addAttribute("kurir", service.getPetugasAktif(null));
        return "dashboard/index";
    }

    @GetMapping("/penjual/notifikasi")
    public String halamanNotifikasiPenjual(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPenjual(session)) {
            ra.addFlashAttribute("flashError", "Silakan login sebagai Penjual terlebih dahulu.");
            return "redirect:/login";
        }
        Penjual penjual = service.getPenjualAktif((Long) session.getAttribute("userId"));
        service.tandaiSemuaNotifikasiDibaca("PENJUAL", penjual.getIdPenjual());
        model.addAttribute("userRole", "PENJUAL");
        model.addAttribute("penjual", penjual);
        model.addAttribute("notifikasiList", service.getNotifikasiPenjual(penjual.getIdPenjual()));
        model.addAttribute("unreadNotif", 0L);
        return "dashboard/notifikasi";
    }

    @GetMapping("/penjual/profile")
    public String halamanProfilPenjual(HttpSession session, Model model, RedirectAttributes ra) {
        if (!isPenjual(session)) {
            ra.addFlashAttribute("flashError", "Silakan login sebagai Penjual terlebih dahulu.");
            return "redirect:/login";
        }
        Penjual penjual = service.getPenjualAktif((Long) session.getAttribute("userId"));
        model.addAttribute("userRole", "PENJUAL");
        model.addAttribute("penjual", penjual);
        model.addAttribute("unreadNotif", service.getUnreadNotifCount("PENJUAL", penjual.getIdPenjual()));
        return "dashboard/profile";
    }

    @PostMapping("/penjual/profile/update")
    public String updateProfilPenjual(@RequestParam String namaPenjual,
                                      @RequestParam String email,
                                      @RequestParam String noHp,
                                      @RequestParam String kategoriPenjual,
                                      @RequestParam String alamat,
                                      @RequestParam String metodePembayaran,
                                      @RequestParam String namaPenyedia,
                                      @RequestParam String noRekening,
                                      @RequestParam(required = false) String passwordBaru,
                                      HttpSession session, RedirectAttributes ra) {
        if (!isPenjual(session)) return "redirect:/login";
        try {
            Long idPenjual = (Long) session.getAttribute("userId");
            service.updateProfilPenjual(idPenjual, namaPenjual, email, noHp, kategoriPenjual, alamat,
                    metodePembayaran, namaPenyedia, noRekening, passwordBaru);
            session.setAttribute("userName", namaPenjual);
            ra.addFlashAttribute("flashSuccess", "Profil & rekening pencairan Anda berhasil diperbarui!");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal memperbarui profil: " + e.getMessage());
        }
        return "redirect:/penjual/profile";
    }

    @PostMapping("/penjual/order")
    public String submitOrderPickup(@RequestParam Double jumlahMinyak,
                                    @RequestParam(defaultValue = "SAAT_INI") String tipeAlamatOrder,
                                    @RequestParam(required = false) String lokasiKustom,
                                    @RequestParam String metodePembayaran,
                                    @RequestParam String namaPenyedia,
                                    @RequestParam String noRekeningTujuan,
                                    @RequestParam(required = false) String catatan,
                                    HttpSession session, RedirectAttributes ra) {
        if (!isPenjual(session)) return "redirect:/login";
        try {
            Penjual penjual = service.getPenjualAktif((Long) session.getAttribute("userId"));
            String lokasiFinal = ("KUSTOM".equalsIgnoreCase(tipeAlamatOrder) && lokasiKustom != null && !lokasiKustom.isBlank())
                    ? lokasiKustom.trim()
                    : penjual.getAlamat();
            if (lokasiFinal == null || lokasiFinal.isBlank()) {
                throw new IllegalArgumentException("Alamat penjemputan tidak boleh kosong!");
            }
            Penjualan baru = service.buatPesananPickup(penjual.getIdPenjual(), jumlahMinyak, lokasiFinal, catatan,
                    metodePembayaran, namaPenyedia, noRekeningTujuan);
            ra.addFlashAttribute("flashSuccess", "Pesanan Pickup (" + jumlahMinyak.intValue() + " Liter) berhasil dibuat! Nomor Resi Anda: " + baru.getKodeResi());
            return "redirect:/check-it-now?tab=tracker&resi=" + baru.getKodeResi();
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/check-it-now?tab=order";
        }
    }

    @PostMapping("/penjual/redeem")
    public String redeemProduk(@RequestParam Long produkId,
                               @RequestParam(defaultValue = "1") int qty,
                               @RequestParam(defaultValue = "SAAT_INI") String tipeAlamatRedeem,
                               @RequestParam(required = false) String alamatKustom,
                               @RequestParam(required = false) String catatanRedeem,
                               HttpSession session, RedirectAttributes ra) {
        if (!isPenjual(session)) {
            ra.addFlashAttribute("flashError", "Silakan login sebagai Penjual terlebih dahulu untuk menukar poin.");
            return "redirect:/login";
        }
        try {
            Penjual penjual = service.getPenjualAktif((Long) session.getAttribute("userId"));
            String alamatFinal = ("KUSTOM".equalsIgnoreCase(tipeAlamatRedeem) && alamatKustom != null && !alamatKustom.isBlank())
                    ? alamatKustom.trim()
                    : penjual.getAlamat();
            if (alamatFinal == null || alamatFinal.isBlank()) {
                throw new IllegalArgumentException("Alamat pengantaran tidak boleh kosong!");
            }
            PenukaranPoin rdm = service.requestRedeemPoin(penjual.getIdPenjual(), produkId, qty, alamatFinal, catatanRedeem);
            ra.addFlashAttribute("flashSuccess", "Permintaan redeem poin berhasil dibuat! Nomor Resi: " + rdm.getKodeResi() + ". Menunggu konfirmasi dari Pusat.");
            return "redirect:/check-it-now?tab=tracker&resi=" + rdm.getKodeResi();
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/check-it-now?tab=redeem";
        }
    }
}
