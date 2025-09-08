package de.fabiexe.spind.client.service;

/**
 * Exception thrown by SpindService operations.
 */
public class SpindServiceException extends Exception {
    
    public SpindServiceException(String message) {
        super(message);
    }
    
    public SpindServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}