package de.fabiexe.spind.client.service;

import de.fabiexe.spind.client.Server;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for HTTP operations with Spind servers.
 * Abstracts away the HTTP client implementation.
 */
public interface SpindHttpService {
    
    /**
     * Downloads the password safe from the server.
     * @param server The server to connect to
     * @param secret The authentication secret
     * @return The encrypted safe data
     * @throws SpindHttpException If the HTTP request fails
     */
    byte @NotNull [] downloadSafe(@NotNull Server server, @NotNull String secret) throws SpindHttpException;
    
    /**
     * Uploads the password safe to the server.
     * @param server The server to connect to
     * @param secret The authentication secret
     * @param safeData The encrypted safe data to upload
     * @throws SpindHttpException If the HTTP request fails
     */
    void uploadSafe(@NotNull Server server, @NotNull String secret, byte @NotNull [] safeData) throws SpindHttpException;
    
    /**
     * Result of an unlock attempt.
     */
    enum UnlockResult {
        SUCCESS,
        INVALID_PASSWORD,
        UNSUPPORTED_VERSION,
        NETWORK_ERROR
    }
    
    /**
     * Exception thrown by HTTP operations.
     */
    class SpindHttpException extends Exception {
        private final int statusCode;
        
        public SpindHttpException(String message) {
            super(message);
            this.statusCode = -1;
        }
        
        public SpindHttpException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public SpindHttpException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = -1;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
}