package com.example.ecommerce.exception;

/**
 * Raised when a customer tries to review a product they have not purchased,
 * or one they have already reviewed.
 *
 * <p>Mapped to 403 Forbidden by {@code GlobalExceptionHandler}, which is one of
 * the two responses the Phase 4 acceptance criteria allow for this case.
 */
public class ReviewNotAllowedException extends RuntimeException {

    public ReviewNotAllowedException(String message) {
        super(message);
    }
}
