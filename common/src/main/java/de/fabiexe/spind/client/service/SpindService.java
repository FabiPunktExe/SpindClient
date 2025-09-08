package de.fabiexe.spind.client.service;

import de.fabiexe.spind.client.Password;
import de.fabiexe.spind.client.Server;
import de.fabiexe.spind.client.repository.ServerRepository;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main service class that coordinates all Spind operations.
 * Provides a high-level API for managing servers and passwords.
 */
public class SpindService {
    
    private final ServerRepository serverRepository;
    private final CryptographyService cryptographyService;
    private final SpindHttpService httpService;
    private final Map<Server, UnlockedSafe> unlockedSafes = new HashMap<>();
    
    /**
     * Represents an unlocked password safe in memory.
     */
    private static class UnlockedSafe {
        final String passwordHash;
        final String secret;
        final List<Password> passwords;
        
        UnlockedSafe(String passwordHash, String secret, List<Password> passwords) {
            this.passwordHash = passwordHash;
            this.secret = secret;
            this.passwords = new ArrayList<>(passwords);
        }
    }
    
    public SpindService(ServerRepository serverRepository, 
                       CryptographyService cryptographyService,
                       SpindHttpService httpService) {
        this.serverRepository = serverRepository;
        this.cryptographyService = cryptographyService;
        this.httpService = httpService;
    }
    
    // Server management methods
    
    public @NotNull List<Server> getServers() {
        return serverRepository.getServers();
    }
    
    public boolean setServers(@NotNull List<Server> servers) {
        return serverRepository.setServers(servers);
    }
    
    // Safe locking/unlocking methods
    
    public boolean isLocked(@NotNull Server server) {
        return !unlockedSafes.containsKey(server);
    }
    
    public boolean unlock(@NotNull Server server, @NotNull String password) throws SpindServiceException {
        try {
            if (!isLocked(server)) {
                return true;
            }
            
            String passwordHash = cryptographyService.hashPassword(password);
            String secret = cryptographyService.hashPassword(passwordHash);
            
            byte[] safeData = httpService.downloadSafe(server, secret);
            List<Password> passwords = cryptographyService.readSafe(passwordHash, safeData);
            
            if (passwords == null) {
                return false; // Invalid password
            }
            
            unlockedSafes.put(server, new UnlockedSafe(passwordHash, secret, passwords));
            return true;
            
        } catch (CryptographyException e) {
            throw new SpindServiceException("Cryptography error during unlock", e);
        } catch (SpindHttpService.SpindHttpException e) {
            if (e.getStatusCode() == 412) {
                return false; // Invalid password
            }
            throw new SpindServiceException("Network error during unlock", e);
        }
    }
    
    public void lock(@NotNull Server server) {
        unlockedSafes.remove(server);
    }
    
    public void setup(@NotNull Server server, @NotNull String password) throws SpindServiceException {
        try {
            String passwordHash = cryptographyService.hashPassword(password);
            String secret = cryptographyService.hashPassword(passwordHash);
            
            byte[] safeData = cryptographyService.writeSafe(passwordHash, List.of());
            httpService.uploadSafe(server, secret, safeData);
            
        } catch (CryptographyException e) {
            throw new SpindServiceException("Cryptography error during setup", e);
        } catch (SpindHttpService.SpindHttpException e) {
            throw new SpindServiceException("Network error during setup", e);
        }
    }
    
    // Password management methods
    
    public @NotNull List<Password> getPasswords(@NotNull Server server) throws SpindServiceException {
        UnlockedSafe safe = unlockedSafes.get(server);
        if (safe == null) {
            throw new SpindServiceException("Safe is not unlocked for server: " + server);
        }
        return new ArrayList<>(safe.passwords);
    }
    
    public boolean setPasswords(@NotNull Server server, @NotNull List<Password> passwords) throws SpindServiceException {
        try {
            UnlockedSafe safe = unlockedSafes.get(server);
            if (safe == null) {
                throw new SpindServiceException("Safe is not unlocked for server: " + server);
            }
            
            byte[] safeData = cryptographyService.writeSafe(safe.passwordHash, passwords);
            httpService.uploadSafe(server, safe.secret, safeData);
            
            safe.passwords.clear();
            safe.passwords.addAll(passwords);
            return true;
            
        } catch (CryptographyException e) {
            throw new SpindServiceException("Cryptography error during password update", e);
        } catch (SpindHttpService.SpindHttpException e) {
            throw new SpindServiceException("Network error during password update", e);
        }
    }
    
    // Utility methods
    
    public int generate2FACode(@NotNull String secret) {
        return cryptographyService.generate2FACode(secret);
    }
}