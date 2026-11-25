package com.example.demo.config;

// =========================================================================
// KONSEP OOP PADA KONFIGURASI INI:
// 1. POLYMORPHISM (Method Overriding): Meng-override method bawaan interface
//    WebMvcConfigurer.addResourceHandlers() dan addInterceptors() untuk
//    menyesuaikan perilaku mapping folder static dan proteksi sesi no-cache.
// =========================================================================
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);

                String uri = request.getRequestURI();
                String method = request.getMethod();

                // Pendaftaran Google yang belum selesai otomatis batal jika pengguna meninggalkan
                // halaman lengkapi profil (misalnya menekan tombol Back) -> tidak ada sesi login.
                HttpSession cekSession = request.getSession(false);
                if (cekSession != null && cekSession.getAttribute("pendingGoogle") != null
                        && !uri.startsWith("/auth/lengkapi-profil-penjual")
                        && !uri.startsWith("/auth/google")
                        && !uri.startsWith("/css/") && !uri.startsWith("/js/")
                        && !uri.startsWith("/images/") && !uri.startsWith("/img/") && !uri.startsWith("/uploads/")
                        && !uri.equals("/favicon.ico") && !uri.equals("/error")) {
                    cekSession.removeAttribute("pendingGoogle");
                }

                // Proteksi REST JSON API (/api/**)
                if (uri.startsWith("/api/")) {
                    boolean publik = uri.equals("/api/auth/login")
                            || ("GET".equals(method) && (uri.equals("/api/overview") || uri.equals("/api/harga-minyak")
                                || uri.equals("/api/stok") || uri.equals("/api/produk")));
                    if (publik) return true;

                    HttpSession apiSession = request.getSession(false);
                    Object role = apiSession != null ? apiSession.getAttribute("userRole") : null;
                    if (apiSession == null || apiSession.getAttribute("userId") == null || role == null) {
                        kirimJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Silakan login terlebih dahulu.");
                        return false;
                    }
                    // Ubah/hapus produk dan lihat data petugas khusus PUSAT
                    boolean khususPusat = (uri.startsWith("/api/produk") && !"GET".equals(method))
                            || uri.startsWith("/api/petugas")
                            || ("GET".equals(method) && (uri.equals("/api/transaksi") || uri.equals("/api/redeem")));
                    if (khususPusat && !"PUSAT".equals(role)) {
                        kirimJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Akses ditolak: khusus akun Pusat.");
                        return false;
                    }
                    // Penjual hanya boleh membuat transaksi/redeem atas namanya sendiri (dicek di ApiController)
                    return true;
                }
                if (uri.startsWith("/dashboard") || uri.startsWith("/check-it-now") || uri.startsWith("/penjual") || uri.startsWith("/petugas") || uri.startsWith("/pusat") || uri.startsWith("/transaksi/bukti")) {
                    HttpSession session = request.getSession(false);
                    if (session == null || session.getAttribute("userId") == null || session.getAttribute("userRole") == null) {
                        response.sendRedirect(request.getContextPath() + "/login");
                        return false;
                    }
                }
                return true;
            }
        });
    }

    private static void kirimJsonError(HttpServletResponse response, int status, String pesan) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"" + pesan + "\"}");
    }
}