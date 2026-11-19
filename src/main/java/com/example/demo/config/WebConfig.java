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
}
