package com.example.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}