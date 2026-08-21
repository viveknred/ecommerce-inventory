package com.example.ecommerce.audit;

import java.util.UUID;

public final class AuditContext {

    private static final ThreadLocal<String> OPERATION_ID =
            new ThreadLocal<>();

    private AuditContext() {
    }

    public static String getOrCreateOperationId() {

        String operationId = OPERATION_ID.get();

        if (operationId == null) {
            operationId = UUID.randomUUID().toString();
            OPERATION_ID.set(operationId);
        }

        return operationId;
    }

    public static void clear() {
        OPERATION_ID.remove();
    }
}