package com.example.ecommerce.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.entity.Coupon;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CouponRepository;
import com.example.ecommerce.repository.ProductRepository;

@Service
public class ScheduledTaskService {

    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public ScheduledTaskService(
            CouponRepository couponRepository,
            ProductRepository productRepository,
            AuditService auditService) {

        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireCoupons() {

        LocalDateTime now =
                LocalDateTime.now();

        List<Coupon> expiredCoupons =
                couponRepository
                        .findByIsActiveTrueAndExpirationDateBefore(
                                now
                        );

        for (Coupon coupon : expiredCoupons) {

            coupon.setIsActive(false);

            couponRepository.save(coupon);

            Map<String, Object> details =
                    new HashMap<>();

            details.put(
                    "couponId",
                    coupon.getId()
            );

            details.put(
                    "couponCode",
                    coupon.getCode()
            );

            details.put(
                    "expirationDate",
                    coupon.getExpirationDate()
            );

            details.put(
                    "expiredAt",
                    now
            );

            auditService.log(
                    "Coupon",
                    "COUPON_EXPIRED",
                    details
            );

            System.out.println(
                    "Scheduled job expired coupon: "
                            + coupon.getCode()
            );
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional(readOnly = true)
    public void checkLowStock() {

        List<Product> lowStockProducts =
                productRepository
                        .findByStockLessThan(5);

        for (Product product : lowStockProducts) {

            Map<String, Object> details =
                    new HashMap<>();

            details.put(
                    "productId",
                    product.getId()
            );

            details.put(
                    "productName",
                    product.getName()
            );

            details.put(
                    "currentStock",
                    product.getStock()
            );

            details.put(
                    "threshold",
                    5
            );

            auditService.log(
                    "Product",
                    "LOW_STOCK_ALERT",
                    details
            );

            System.out.println(
                    "LOW STOCK ALERT: Product #"
                            + product.getId()
                            + " ("
                            + product.getName()
                            + ") has "
                            + product.getStock()
                            + " unit(s) remaining."
            );
        }
    }
}