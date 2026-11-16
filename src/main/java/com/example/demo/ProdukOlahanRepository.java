package com.example.demo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdukOlahanRepository extends JpaRepository<ProdukOlahan, Long> {
    List<ProdukOlahan> findAllByOrderByIdProdukAsc();
}
