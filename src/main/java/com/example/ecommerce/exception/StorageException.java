package com.example.ecommerce.exception;

/**
 * Raised when a valid file cannot be written to or read from local disk,
 * for example on a permissions or I/O failure.
 *
 * <p>Mapped to 500 Internal Server Error: the request was well-formed, the
 * server simply could not fulfil it.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(String message) {
        super(message);
    }
}
