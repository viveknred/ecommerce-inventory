package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.ecommerce.dto.CouponRequest;
import com.example.ecommerce.dto.CouponResponse;
import com.example.ecommerce.entity.Coupon;
import com.example.ecommerce.exception.InvalidCouponException;
import com.example.ecommerce.repository.CouponRepository;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final AuditService auditService;

    public CouponService(
            CouponRepository couponRepository,
            AuditService auditService) {

        this.couponRepository = couponRepository;
        this.auditService = auditService;
    }

    public CouponResponse create(
            CouponRequest request) {

        if (couponRepository
                .findByCode(request.getCode())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Coupon code already exists"
            );
        }

        Coupon coupon = new Coupon();

        coupon.setCode(request.getCode());
        coupon.setDiscountPercent(
                request.getDiscountPercent()
        );

        coupon.setExpirationDate(
                request.getExpirationDate()
        );

        coupon.setIsActive(
                request.getIsActive() == null
                        ? true
                        : request.getIsActive()
        );

        Coupon savedCoupon =
                couponRepository.save(coupon);

        Map<String, Object> details =
                new HashMap<>();

        details.put(
                "couponId",
                savedCoupon.getId()
        );

        details.put(
                "code",
                savedCoupon.getCode()
        );

        details.put(
                "discountPercent",
                savedCoupon.getDiscountPercent()
        );

        details.put(
                "expirationDate",
                savedCoupon.getExpirationDate()
        );

        details.put(
                "isActive",
                savedCoupon.getIsActive()
        );

        auditService.log(
                "Coupon",
                "COUPON_CREATED",
                details
        );

        return toResponse(
                savedCoupon
        );
    }

    public Coupon getValidCoupon(
            String code) {

        if (code == null
                || code.isBlank()) {

            throw new InvalidCouponException(
                    "Coupon code cannot be blank"
            );
        }

        Coupon coupon =
                couponRepository
                        .findByCode(
                                code.trim()
                        )
                        .orElseThrow(() ->
                                new InvalidCouponException(
                                        "Invalid coupon: "
                                                + code
                                ));

        if (!Boolean.TRUE.equals(
                coupon.getIsActive())) {

            throw new InvalidCouponException(
                    "Coupon is inactive"
            );
        }

        if (coupon.getExpirationDate() == null
                || !coupon.getExpirationDate()
                        .isAfter(
                                LocalDateTime.now()
                        )) {

            throw new InvalidCouponException(
                    "Coupon has expired"
            );
        }

        return coupon;
    }

    public BigDecimal applyDiscount(
            BigDecimal amount,
            Coupon coupon) {

        BigDecimal discount =
                amount.multiply(
                        BigDecimal.valueOf(
                                coupon.getDiscountPercent()
                        )
                ).divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP
                );

        return amount.subtract(
                discount
        );
    }

    private CouponResponse toResponse(
            Coupon coupon) {

        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDiscountPercent(),
                coupon.getExpirationDate(),
                coupon.getIsActive()
        );
    }
}