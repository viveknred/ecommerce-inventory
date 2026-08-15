package com.example.ecommerce.audit;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.ecommerce.entity.AuditLog;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.AuditLogRepository;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper) {

        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @AfterReturning(
            pointcut = "@annotation(auditAction)",
            returning = "result")
    public void audit(
            JoinPoint joinPoint,
            AuditAction auditAction,
            Object result) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String changedBy = authentication != null
                ? authentication.getName()
                : "SYSTEM";

        AuditLog auditLog = new AuditLog();

        auditLog.setEntityName(
                auditAction.entity()
        );

        auditLog.setAction(
                auditAction.action()
        );

        auditLog.setChangedBy(changedBy);

        auditLog.setDetails(
                buildDetails(
                        joinPoint,
                        result,
                        auditAction
                )
        );

        auditLogRepository.save(auditLog);
    }

    private String buildDetails(
            JoinPoint joinPoint,
            Object result,
            AuditAction auditAction) {

        Map<String, Object> details =
                new HashMap<>();

        details.put(
                "method",
                joinPoint.getSignature().getName()
        );

        details.put(
                "action",
                auditAction.action()
        );

        Object[] arguments =
                joinPoint.getArgs();

        if (result instanceof Order order) {

            details.put(
                    "orderId",
                    order.getId()
            );

            details.put(
                    "status",
                    order.getStatus()
            );

            if (arguments.length > 1
                    && arguments[1] != null) {

                details.put(
                        "newStatus",
                        arguments[1].toString()
                );
            }

        } else if (result instanceof Product product) {

            details.put(
                    "productId",
                    product.getId()
            );

            details.put(
                    "name",
                    product.getName()
            );

            details.put(
                    "price",
                    product.getPrice()
            );

            details.put(
                    "stock",
                    product.getStock()
            );

            if (arguments.length > 1
                    && arguments[1] instanceof Integer quantity) {

                details.put(
                        "stockChange",
                        quantity
                );
            }
        }

        try {

            return objectMapper.writeValueAsString(
                    details
            );

        } catch (JacksonException ex) {

            return "{\"message\":\"Unable to serialize audit details\"}";
        }
    }
}