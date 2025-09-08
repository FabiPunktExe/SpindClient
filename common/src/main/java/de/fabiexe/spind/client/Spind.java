package de.fabiexe.spind.client;

import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Legacy API facade for Spind operations.
 * This class maintains backward compatibility with the original static API
 * while delegating all operations to the refactored service layer.
 * 
 * @deprecated Use SpindServiceFactory.getInstance() for better testability and dependency injection.
 */
@Deprecated
public class Spind {
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static @NotNull List<Server> getServers() {
        return SpindCompatibility.getServers();
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static boolean setServers(@NotNull List<Server> servers) {
        return SpindCompatibility.setServers(servers);
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static boolean isLocked(@NotNull Server server) {
        return SpindCompatibility.isLocked(server);
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static boolean unlock(@NotNull Server server, @NotNull String password) {
        return SpindCompatibility.unlock(server, password);
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static void lock(@NotNull Server server) {
        SpindCompatibility.lock(server);
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static void setup(@NotNull Server server, @NotNull String password) {
        SpindCompatibility.setup(server, password);
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static @NotNull List<Password> getPasswords(@NotNull Server server) {
        return SpindCompatibility.getPasswords(server);
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static boolean setPasswords(@NotNull Server server, @NotNull List<Password> passwords) {
        return SpindCompatibility.setPasswords(server, passwords);
    }
    
    /**
     * @deprecated Use SpindService directly through SpindServiceFactory.getInstance()
     */
    @Deprecated
    public static int twoFA(@NotNull String secret) {
        return SpindCompatibility.twoFA(secret);
    }
}
