package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    /**
     * Phase 4: every account linked to one marketplace vendor.
     */
    List<User> findByVendorId(Long vendorId);

    /**
     * Phase 4: the first account bound to a vendor, treated as its owner
     * for display purposes.
     */
    Optional<User> findFirstByVendorIdOrderByIdAsc(Long vendorId);
}
