package de.fabiexe.spind.client.service;

/**
 * Exception thrown by cryptography operations.
 */
public class CryptographyException extends Exception {
    
    public CryptographyException(String message) {
        super(message);
    }
    
    public CryptographyException(String message, Throwable cause) {
        super(message, cause);
    }
}