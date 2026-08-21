package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.audit.AuditContext;
import com.example.ecommerce.entity.AuditLog;
import com.example.ecommerce.repository.AuditLogRepository;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper) {

        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            String entityName,
            String action,
            Map<String, Object> details) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String changedBy =
                authentication != null
                        ? authentication.getName()
                        : "SYSTEM";

        Map<String, Object> auditDetails =
                new LinkedHashMap<>();

        auditDetails.put(
                "description",
                buildDescription(
                        entityName,
                        action,
                        details
                )
        );

        auditDetails.putAll(details);

        AuditLog auditLog = new AuditLog();

        auditLog.setOperationId(
                AuditContext.getOrCreateOperationId()
        );

        auditLog.setEntityName(entityName);
        auditLog.setAction(action);
        auditLog.setChangedBy(changedBy);

        try {

            auditLog.setDetails(
                    objectMapper.writeValueAsString(
                            auditDetails
                    )
            );

        } catch (JacksonException ex) {

            auditLog.setDetails(
                    "{\"description\":\"Unable to serialize audit details\"}"
            );
        }

        auditLogRepository.save(auditLog);
    }

    private String buildDescription(
            String entityName,
            String action,
            Map<String, Object> details) {

        switch (action) {

            case "ORDER_CREATED":

                return String.format(
                        "Order #%s was created by %s for user ID %s. "
                                + "The order total is %s, contains %s item(s), "
                                + "and was created with %s status.",
                        value(details, "orderId"),
                        value(details, "email"),
                        value(details, "userId"),
                        formatAmount(details.get("totalAmount")),
                        value(details, "itemCount"),
                        value(details, "status")
                );

            case "STATUS_UPDATE":

                return String.format(
                        "Order #%s status changed from %s to %s.",
                        value(details, "orderId"),
                        value(details, "oldStatus"),
                        value(details, "newStatus")
                );
            case "STOCK_DEDUCTED":

                return String.format(
                        "Stock for product #%s (%s) was reduced by %s unit(s). "
                                + "Previous stock was %s and current stock is %s.",
                        value(details, "productId"),
                        value(details, "productName"),
                        absoluteValue(
                                details.get("quantityChanged")
                        ),
                        value(details, "oldStock"),
                        value(details, "newStock")
                );

            case "STOCK_RESTORED":

                return String.format(
                        "Stock for product #%s (%s) was restored by %s unit(s). "
                                + "Previous stock was %s and current stock is %s.",
                        value(details, "productId"),
                        value(details, "productName"),
                        value(details, "quantityChanged"),
                        value(details, "oldStock"),
                        value(details, "newStock")
                );

            case "PRODUCT_CREATED":

                return String.format(
                        "Product #%s (%s) was created with category %s, "
                                + "price %s, and initial stock of %s unit(s).",
                        value(details, "productId"),
                        value(details, "name"),
                        value(details, "category"),
                        formatAmount(details.get("price")),
                        value(details, "stock")
                );

            case "PRODUCT_UPDATED":

                return String.format(
                        "Product #%s was updated. Stock changed from %s to %s "
                                + "and price changed from %s to %s.",
                        value(details, "productId"),
                        value(details, "oldStock"),
                        value(details, "newStock"),
                        formatAmount(details.get("oldPrice")),
                        formatAmount(details.get("newPrice"))
                );

            case "PRODUCT_DELETED":

                return String.format(
                        "Product #%s (%s) was deleted. "
                                + "The product had price %s and stock of %s unit(s).",
                        value(details, "productId"),
                        value(details, "name"),
                        formatAmount(details.get("price")),
                        value(details, "stock")
                );

            case "COUPON_CREATED":

                return String.format(
                        "Coupon %s was created with a %s%% discount. "
                                + "It is active: %s and expires on %s.",
                        value(details, "code"),
                        value(details, "discountPercent"),
                        value(details, "isActive"),
                        value(details, "expirationDate")
                );

            case "COUPON_APPLIED":

                return String.format(
                        "Coupon %s was applied, providing a %s%% discount. "
                                + "The discounted order total is %s.",
                        value(details, "couponCode"),
                        value(details, "discountPercent"),
                        formatAmount(
                                details.get("discountedTotal")
                        )
                );

            case "PAYMENT_SUCCESS":

                return String.format(
                        "Payment #%s for order #%s was successfully processed "
                                + "for %s. Transaction ID: %s.",
                        value(details, "paymentId"),
                        value(details, "orderId"),
                        formatAmount(
                                details.get("amount")
                        ),
                        value(details, "transactionId")
                );

            default:

                return String.format(
                        "%s action '%s' was recorded.",
                        entityName,
                        action
                );
        }
    }

    private String value(
            Map<String, Object> details,
            String key) {

        Object value = details.get(key);

        return value == null
                ? "N/A"
                : String.valueOf(value);
    }

    private String absoluteValue(Object value) {

        if (value instanceof Number number) {
            return String.valueOf(
                    Math.abs(number.longValue())
            );
        }

        return value == null
                ? "N/A"
                : String.valueOf(value);
    }

    private String formatAmount(Object value) {

        if (value instanceof BigDecimal amount) {

            NumberFormat formatter =
                    NumberFormat.getNumberInstance(
                            Locale.US
                    );

            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            return "₹" + formatter.format(amount);
        }

        if (value instanceof Number number) {

            return "₹" + String.format(
                    Locale.US,
                    "%.2f",
                    number.doubleValue()
            );
        }

        return value == null
                ? "N/A"
                : String.valueOf(value);
    }
}