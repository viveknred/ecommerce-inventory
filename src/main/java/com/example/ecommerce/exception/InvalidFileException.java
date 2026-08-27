package com.example.ecommerce.exception;

/**
 * Raised when an uploaded file fails validation: wrong MIME type, wrong
 * extension, empty payload, or larger than the configured limit.
 *
 * <p>Mapped to 400 Bad Request with a human-readable message.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
