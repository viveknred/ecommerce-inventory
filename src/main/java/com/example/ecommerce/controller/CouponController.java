package com.example.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.dto.CouponRequest;
import com.example.ecommerce.dto.CouponResponse;
import com.example.ecommerce.service.CouponService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(
            @Valid @RequestBody CouponRequest request) {

        return couponService.create(request);
    }
}