package de.fabiexe.spind.client;

import de.fabiexe.spind.client.service.SpindService;
import de.fabiexe.spind.client.service.SpindServiceException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Compatibility wrapper for the original Spind static API.
 * Delegates all calls to the new service-based implementation.
 * This ensures backward compatibility while benefiting from the refactored architecture.
 * 
 * @deprecated Use SpindServiceFactory.getInstance() directly for better testability and dependency injection.
 */
@Deprecated
public class SpindCompatibility {
    
    private static SpindService getService() {
        return SpindServiceFactory.getInstance();
    }
    
    public static @NotNull List<Server> getServers() {
        return getService().getServers();
    }
    
    public static boolean setServers(@NotNull List<Server> servers) {
        return getService().setServers(servers);
    }
    
    public static boolean isLocked(@NotNull Server server) {
        return getService().isLocked(server);
    }
    
    public static boolean unlock(@NotNull Server server, @NotNull String password) {
        try {
            return getService().unlock(server, password);
        } catch (SpindServiceException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public static void lock(@NotNull Server server) {
        getService().lock(server);
    }
    
    public static void setup(@NotNull Server server, @NotNull String password) {
        try {
            getService().setup(server, password);
        } catch (SpindServiceException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public static @NotNull List<Password> getPasswords(@NotNull Server server) {
        try {
            return getService().getPasswords(server);
        } catch (SpindServiceException e) {
            e.printStackTrace(System.err);
            return List.of();
        }
    }
    
    public static boolean setPasswords(@NotNull Server server, @NotNull List<Password> passwords) {
        try {
            return getService().setPasswords(server, passwords);
        } catch (SpindServiceException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public static int twoFA(@NotNull String secret) {
        return getService().generate2FACode(secret);
    }
}