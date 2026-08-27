package com.example.ecommerce.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.ecommerce.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
                JpaSpecificationExecutor<Product> {

    List<Product> findByStockLessThan(Integer stock);

    Page<Product> findByVendorId(Long vendorId, Pageable pageable);

    boolean existsByVendorId(Long vendorId);
}
