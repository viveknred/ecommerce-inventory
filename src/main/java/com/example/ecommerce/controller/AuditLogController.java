package com.example.ecommerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.entity.AuditLog;
import com.example.ecommerce.repository.AuditLogRepository;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLog> getAuditLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "id,desc") String sort) {

        String[] sortParts = sort.split(",");

        String property = sortParts[0];
        Sort.Direction direction = Sort.Direction.DESC;

        if (sortParts.length > 1) {
            direction = Sort.Direction.fromString(sortParts[1]);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, property)
        );

        return auditLogRepository.findAll(pageable);
    }
}