package com.example.demo.controller;

// =========================================================================
// KONSEP OOP PADA CONTROLLER INI:
// 1. SEPARATION OF CONCERNS & COMPOSITION: Memisahkan tanggung jawab modul
//    autentikasi (Login lokal, Login Google OAuth2, Rate Limiting 5x/menit
//    cooldown 30 detik, Register, Onboarding), API histori log update, serta
//    endpoint template Bukti Pembayaran PDF melalui Constructor Injection.
// =========================================================================
import com.example.demo.entity.*;
import com.example.demo.repository.PenjualRepository;
import com.example.demo.service.InputValidator;
import com.example.demo.service.JelantaraService;
import com.example.demo.service.LoginRateLimiterService;
import com.example.demo.service.PasswordEncoderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    private final JelantaraService service;
    private final PenjualRepository penjualRepo;
    private final LoginRateLimiterService rateLimiter;
    private final PasswordEncoderService passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.client.id:}")
    private String googleClientId;

    public AuthController(JelantaraService service, PenjualRepository penjualRepo,
                          LoginRateLimiterService rateLimiter,
                          PasswordEncoderService passwordEncoder) {
        this.service = service;
        this.penjualRepo = penjualRepo;
        this.rateLimiter = rateLimiter;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isGoogleConfigured() {
        return googleClientId != null && !googleClientId.isBlank()
                && !googleClientId.startsWith("YOUR_GOOGLE_CLIENT_ID");
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
    public String loginPage(HttpSession session, HttpServletRequest request, Model model) {
        String role = (String) session.getAttribute("userRole");
        if ("PUSAT".equalsIgnoreCase(role)) return "redirect:/pusat";
        if ("PETUGAS".equalsIgnoreCase(role)) return "redirect:/petugas";
        if ("PENJUAL".equalsIgnoreCase(role)) return "redirect:/check-it-now";

        String rateKey = request.getRemoteAddr();
        model.addAttribute("googleClientId", googleClientId);
        model.addAttribute("googleConfigured", isGoogleConfigured());
        model.addAttribute("cooldownSeconds", rateLimiter.getRemainingCooldownSeconds(rateKey));
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String processLogin(@RequestParam String email, @RequestParam String password,
                               HttpSession session, HttpServletRequest request, RedirectAttributes ra) {
        String rateKey = request.getRemoteAddr();

        // Cek apakah pengguna sedang dalam masa cooldown 30 detik (akibat salah 5x dalam 1 menit)
        if (rateLimiter.isBlocked(rateKey)) {
            long sisaDetik = rateLimiter.getRemainingCooldownSeconds(rateKey);
            ra.addFlashAttribute("flashError", "Terlalu banyak percobaan login gagal (maksimal 5x dalam 1 menit). Silakan tunggu " + sisaDetik + " detik lagi.");
            ra.addFlashAttribute("cooldownSeconds", sisaDetik);
            return "redirect:/login";
        }

        try {
            email = InputValidator.validateEmail(email);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/login";
        }
        if (password == null || password.isBlank()) {
            ra.addFlashAttribute("flashError", "Password wajib diisi!");
            return "redirect:/login";
        }
        Optional<JelantaraService.AuthSession> auth = service.authenticateUser(email, password);
        if (auth.isEmpty()) {
            int sisaPercobaan = rateLimiter.recordFailedAttempt(rateKey);
            if (sisaPercobaan <= 0) {
                long sisaDetik = rateLimiter.getRemainingCooldownSeconds(rateKey);
                ra.addFlashAttribute("flashError", "Batas 5x percobaan login gagal dalam 1 menit tercapai! Login dikunci selama " + sisaDetik + " detik.");
                ra.addFlashAttribute("cooldownSeconds", sisaDetik);
            } else {
                ra.addFlashAttribute("flashError", "Email atau password salah. Sisa percobaan dalam 1 menit: " + sisaPercobaan + " kali.");
            }
            return "redirect:/login";
        }

        rateLimiter.resetAttempts(rateKey);
        return buatSesiDanRedirect(auth.get(), false, session, ra);
    }

    // Endpoint Callback Autentikasi Google (Mendukung Google Identity Services JWT asli & Mode Fallback)
    @PostMapping("/auth/google")
    public String processGoogleLogin(@RequestParam(required = false) String credential,
                                     @RequestParam(required = false) String googleEmail,
                                     @RequestParam(required = false) String googleName,
                                     HttpSession session, HttpServletRequest request, RedirectAttributes ra) {
        String rateKey = request.getRemoteAddr();
        if (rateLimiter.isBlocked(rateKey)) {
            long sisaDetik = rateLimiter.getRemainingCooldownSeconds(rateKey);
            ra.addFlashAttribute("flashError", "Login sedang dalam masa cooldown. Silakan tunggu " + sisaDetik + " detik lagi.");
            return "redirect:/login";
        }

        try {
            String email;
            String nama;
            String googleId;
            String picture = null;

            if (credential != null && !credential.isBlank()) {
                // Verifikasi ID Token resmi dari Google Identity Services
                String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(credential, StandardCharsets.UTF_8);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest httpReq = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                HttpResponse<String> resp = client.send(httpReq, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() != 200) {
                    throw new IllegalArgumentException("Token Google tidak valid atau sudah kedaluwarsa.");
                }
                JsonNode json = objectMapper.readTree(resp.body());
                email = json.path("email").asText();
                nama = json.path("name").asText();
                googleId = json.path("sub").asText();
                picture = json.has("picture") ? json.path("picture").asText() : null;
            } else if (googleEmail != null && !googleEmail.isBlank()) {
                // Mode simulasi/lokal apabila Google Client ID belum diisi di application.properties
                email = InputValidator.validateEmail(googleEmail);
                nama = (googleName != null && !googleName.isBlank()) ? googleName.trim() : email.split("@")[0];
                googleId = "google-local-" + Math.abs(email.hashCode());
            } else {
                throw new IllegalArgumentException("Data autentikasi Google tidak ditemukan.");
            }

            JelantaraService.GoogleAuthResult result = service.authenticateOrRegisterGoogle(email, nama, googleId, picture);
            rateLimiter.resetAttempts(rateKey);
            return buatSesiDanRedirect(result.session(), result.perluLengkapiProfil(), session, ra);
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Login Google gagal: " + e.getMessage());
            return "redirect:/login";
        }
    }

    private String buatSesiDanRedirect(JelantaraService.AuthSession user, boolean perluLengkapiProfil,
                                       HttpSession session, RedirectAttributes ra) {
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
            if (perluLengkapiProfil) {
                ra.addFlashAttribute("flashSuccess", "Login Google berhasil! Silakan lengkapi alamat dan rekening pencairan Anda.");
                return "redirect:/auth/lengkapi-profil-penjual";
            }
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
    public String registerPage(Model model) {
        model.addAttribute("googleClientId", googleClientId);
        model.addAttribute("googleConfigured", isGoogleConfigured());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String processRegister(@RequestParam String namaPenjual, @RequestParam String email,
                                  @RequestParam String noHp, @RequestParam String password,
                                  @RequestParam(required = false) String alamat,
                                  HttpSession session, RedirectAttributes ra) {
        try {
            String cleanNama = InputValidator.validateNotBlank(namaPenjual, "Nama lengkap");
            String cleanEmail = InputValidator.validateEmail(email);
            String cleanHp = InputValidator.validatePhone(noHp, "Nomor HP / WhatsApp");
            if (password == null || password.length() < 6) {
                throw new IllegalArgumentException("Password wajib diisi minimal 6 karakter.");
            }
            Penjual baru = new Penjual();
            baru.setNamaPenjual(cleanNama);
            baru.setEmail(cleanEmail);
            baru.setNoHp(cleanHp);
            baru.setPassword(passwordEncoder.encodePassword(password));
            baru.setAlamat(alamat != null ? alamat : "");
            baru.setAuthProvider("LOCAL");
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
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/register";
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
                                              @RequestParam(required = false) String noHp,
                                              HttpSession session, RedirectAttributes ra) {
        String role = (String) session.getAttribute("userRole");
        if (!"PENJUAL".equalsIgnoreCase(role)) return "redirect:/login";
        Long userId = (Long) session.getAttribute("userId");
        try {
            Penjual p = service.getPenjualAktif(userId);
            if (p != null) {
                String cleanRek = InputValidator.validateRekening(noRekening);
                String cleanAlamat = InputValidator.validateNotBlank(alamat, "Alamat lengkap");
                p.setKategoriPenjual(kategoriPenjual);
                p.setMetodePembayaran(metodePembayaran);
                p.setNamaPenyedia(namaPenyedia);
                p.setNoRekening(cleanRek);
                p.setAlamat(cleanAlamat);
                if (noHp != null && !noHp.isBlank()) {
                    p.setNoHp(InputValidator.validatePhone(noHp, "Nomor HP / WhatsApp"));
                }
                penjualRepo.save(p);
            }
            ra.addFlashAttribute("flashSuccess", "Profil & rekening pencairan berhasil disimpan! Kini Anda dapat mulai bertransaksi.");
            return "redirect:/check-it-now";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/auth/lengkapi-profil-penjual";
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Gagal menyimpan profil: Nomor HP mungkin sudah terdaftar pada akun lain.");
            return "redirect:/auth/lengkapi-profil-penjual";
        }
    }

    // API JSON untuk mengambil seluruh baris riwayat update transaksi (Append-Only Audit Log)
    @GetMapping("/api/riwayat-update")
    @ResponseBody
    public List<Map<String, Object>> getRiwayatUpdateApi(@RequestParam String jenis, @RequestParam Long id) {
        List<RiwayatUpdateTransaksi> logs = "REDEEM".equalsIgnoreCase(jenis)
                ? service.getRiwayatUpdatePenukaran(id)
                : service.getRiwayatUpdatePenjualan(id);

        List<Map<String, Object>> result = new ArrayList<>();
        for (RiwayatUpdateTransaksi log : logs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idRiwayat", log.getIdRiwayat());
            item.put("status", log.getStatus());
            item.put("aktorRole", log.getAktorRole());
            item.put("namaAktor", log.getNamaAktor());
            item.put("keteranganBukti", log.getKeteranganBukti());
            item.put("fotoBukti", log.getFotoBukti());
            item.put("waktu", log.getWaktuFormatted());
            result.add(item);
        }
        return result;
    }

    // Halaman Template Bukti Pembayaran Penjualan Minyak (Download langsung sebagai file .PDF via html2pdf.js)
    @GetMapping("/transaksi/bukti/{id}")
    public String halamanBuktiPembayaranPdf(@PathVariable Long id,
                                            @RequestParam(defaultValue = "JUAL_MINYAK") String jenis,
                                            HttpSession session, Model model, RedirectAttributes ra) {
        String role = (String) session.getAttribute("userRole");
        Long userId = (Long) session.getAttribute("userId");
        if (role == null) return "redirect:/login";

        if ("REDEEM".equalsIgnoreCase(jenis)) {
            Optional<PenukaranPoin> opt = service.getPenukaranByIdEnriched(id);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("flashError", "Data transaksi redeem tidak ditemukan.");
                return "redirect:/check-it-now?tab=transaction";
            }
            PenukaranPoin rdm = opt.get();
            if ("PENJUAL".equalsIgnoreCase(role) && !rdm.getPenjualId().equals(userId)) {
                ra.addFlashAttribute("flashError", "Akses ditolak! Bukti transaksi bukan milik akun Anda.");
                return "redirect:/check-it-now?tab=transaction";
            }
            model.addAttribute("jenisTransaksi", "REDEEM");
            model.addAttribute("redeem", rdm);
            model.addAttribute("riwayatLog", service.getRiwayatUpdatePenukaran(id));
            model.addAttribute("kodeResi", rdm.getKodeResi());
        } else {
            Optional<Penjualan> opt = service.getPenjualanByIdEnriched(id);
            if (opt.isEmpty()) {
                ra.addFlashAttribute("flashError", "Data transaksi penjualan tidak ditemukan.");
                return "redirect:/check-it-now?tab=transaction";
            }
            Penjualan trx = opt.get();
            if ("PENJUAL".equalsIgnoreCase(role) && !trx.getPenjualId().equals(userId)) {
                ra.addFlashAttribute("flashError", "Akses ditolak! Bukti pembayaran bukan milik akun Anda.");
                return "redirect:/check-it-now?tab=transaction";
            }
            model.addAttribute("jenisTransaksi", "JUAL_MINYAK");
            model.addAttribute("transaksi", trx);
            model.addAttribute("riwayatLog", service.getRiwayatUpdatePenjualan(id));
            model.addAttribute("kodeResi", trx.getKodeResi());
        }

        model.addAttribute("userRole", role);
        model.addAttribute("pusat", service.getPusatAktif());
        return "dashboard/bukti-pembayaran";
    }
}
