package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

// Controller Modular khusus untuk Landing Page Publik & Autentikasi (Login, Register, Lengkapi Profil)
@Controller
public class AuthController {

    private final JelantaraService service;
    private final PenjualRepository penjualRepo;

    public AuthController(JelantaraService service, PenjualRepository penjualRepo) {
        this.service = service;
        this.penjualRepo = penjualRepo;
    }

    @GetMapping({"/", "/home"})
    public String homePage(Model model, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        Long userId = (Long) session.getAttribute("userId");

        Penjual penjualLogin = ("PENJUAL".equalsIgnoreCase(role) && userId != null)
                ? service.getPenjualAktif(userId)
                : null;

        model.addAttribute("userRole", role);
        model.addAttribute("userName", session.getAttribute("userName"));
        model.addAttribute("penjual", penjualLogin);
        model.addAttribute("produkList", service.getSemuaProduk());
        model.addAttribute("hargaAcuan", service.getHargaMinyakBerlaku());
        model.addAttribute("stokGudang", service.getStokTerbaru());
        model.addAttribute("unreadNotif", service.getUnreadNotifCount(role, userId));
        return "home/index";
    }

    @GetMapping({"/login", "/auth/login"})
    public String loginPage(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if ("PUSAT".equalsIgnoreCase(role)) return "redirect:/pusat";
        if ("PETUGAS".equalsIgnoreCase(role)) return "redirect:/petugas";
        if ("PENJUAL".equalsIgnoreCase(role)) return "redirect:/check-it-now";
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String processLogin(@RequestParam String email, @RequestParam String password,
                               HttpSession session, RedirectAttributes ra) {
        Optional<JelantaraService.AuthSession> auth = service.authenticateUser(email, password);
        if (auth.isEmpty()) {
            ra.addFlashAttribute("flashError", "Email atau password salah, atau akses akun Anda sedang dinonaktifkan.");
            return "redirect:/login";
        }

        JelantaraService.AuthSession user = auth.get();
        session.setAttribute("userId", user.id());
        session.setAttribute("userName", user.nama());
        session.setAttribute("userEmail", user.email());
        session.setAttribute("userRole", user.role());

        if ("PUSAT".equals(user.role())) {
            ra.addFlashAttribute("flashSuccess", "Selamat datang di Dashboard Pusat, " + user.nama() + "!");
            return "redirect:/pusat";
        } else if ("PETUGAS".equals(user.role())) {
            ra.addFlashAttribute("flashSuccess", "Login berhasil! Selamat bertugas, " + user.nama() + ".");
            return "redirect:/petugas";
        } else {
            ra.addFlashAttribute("flashSuccess", "Selamat datang kembali, " + user.nama() + "!");
            return "redirect:/check-it-now";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("flashSuccess", "Anda telah keluar dari sesi akun Jelantara.");
        return "redirect:/login";
    }

    @GetMapping({"/register", "/auth/register"})
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String processRegister(@RequestParam String namaPenjual, @RequestParam String email,
                                  @RequestParam String noHp, @RequestParam String password,
                                  @RequestParam(required = false) String alamat,
                                  HttpSession session, RedirectAttributes ra) {
        try {
            Penjual baru = new Penjual();
            baru.setNamaPenjual(namaPenjual);
            baru.setEmail(email.trim());
            baru.setNoHp(noHp.trim());
            baru.setPassword(password);
            baru.setAlamat(alamat != null ? alamat : "");
            baru.setPoin(0);
            Penjual tersimpan = penjualRepo.save(baru);

            session.setAttribute("userId", tersimpan.getIdPenjual());
            session.setAttribute("userName", tersimpan.getNamaPenjual());
            session.setAttribute("userEmail", tersimpan.getEmail());
            session.setAttribute("userRole", "PENJUAL");

            service.buatNotifikasi("Selamat Bergabung di Jelantara!",
                    "Akun penjual atas nama " + tersimpan.getNamaPenjual() + " berhasil didaftarkan. Silakan lengkapi profil dan rekening pencairan Anda.",
                    "REGISTRASI", tersimpan.getIdPenjual(), null, 1L);

            ra.addFlashAttribute("flashSuccess", "Registrasi berhasil! Silakan lengkapi kategori, rekening/e-wallet, dan alamat penjemputan Anda.");
            return "redirect:/auth/lengkapi-profil-penjual";
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal mendaftar: Email atau Nomor HP sudah terdaftar.");
            return "redirect:/register";
        }
    }

    @GetMapping("/auth/lengkapi-profil-penjual")
    public String lengkapiProfilPenjualPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute("userRole");
        if (!"PENJUAL".equalsIgnoreCase(role)) return "redirect:/login";
        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("penjual", service.getPenjualAktif(userId));
        return "auth/lengkapi-profil-penjual";
    }

    @PostMapping("/auth/lengkapi-profil-penjual")
    public String prosesLengkapiProfilPenjual(@RequestParam String kategoriPenjual,
                                              @RequestParam(defaultValue = "Bank") String metodePembayaran,
                                              @RequestParam(defaultValue = "BCA") String namaPenyedia,
                                              @RequestParam String noRekening,
                                              @RequestParam String alamat,
                                              HttpSession session, RedirectAttributes ra) {
        String role = (String) session.getAttribute("userRole");
        if (!"PENJUAL".equalsIgnoreCase(role)) return "redirect:/login";
        Long userId = (Long) session.getAttribute("userId");
        Penjual p = service.getPenjualAktif(userId);
        if (p != null) {
            p.setKategoriPenjual(kategoriPenjual);
            p.setMetodePembayaran(metodePembayaran);
            p.setNamaPenyedia(namaPenyedia);
            p.setNoRekening(noRekening.trim());
            p.setAlamat(alamat.trim());
            penjualRepo.save(p);
        }
        ra.addFlashAttribute("flashSuccess", "Profil & rekening pencairan berhasil disimpan! Kini Anda dapat mulai bertransaksi.");
        return "redirect:/check-it-now";
    }
}
