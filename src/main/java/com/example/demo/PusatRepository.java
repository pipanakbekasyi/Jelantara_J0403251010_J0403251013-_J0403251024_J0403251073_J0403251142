package com.example.demo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PusatRepository extends JpaRepository<Pusat, Long> {
    Optional<Pusat> findByEmail(String email);
}
