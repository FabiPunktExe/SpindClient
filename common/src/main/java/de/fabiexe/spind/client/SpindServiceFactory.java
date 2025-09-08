package de.fabiexe.spind.client;

import de.fabiexe.spind.client.repository.FileServerRepository;
import de.fabiexe.spind.client.repository.ServerRepository;
import de.fabiexe.spind.client.service.CryptographyService;
import de.fabiexe.spind.client.service.OkHttpSpindService;
import de.fabiexe.spind.client.service.SpindHttpService;
import de.fabiexe.spind.client.service.SpindService;
import de.fabiexe.spind.client.service.StandardCryptographyService;

import java.nio.file.Path;

/**
 * Factory class for creating SpindService instances with proper dependency injection.
 * Determines the appropriate data directory based on the operating system.
 */
public class SpindServiceFactory {
    
    private static volatile SpindService instance;
    
    /**
     * Gets a singleton instance of SpindService.
     * @return Configured SpindService instance
     */
    public static SpindService getInstance() {
        if (instance == null) {
            synchronized (SpindServiceFactory.class) {
                if (instance == null) {
                    instance = createSpindService();
                }
            }
        }
        return instance;
    }
    
    /**
     * Creates a new SpindService instance with default configuration.
     * @return Configured SpindService instance
     */
    public static SpindService createSpindService() {
        Path dataDirectory = getDataDirectory();
        return createSpindService(dataDirectory);
    }
    
    /**
     * Creates a new SpindService instance with custom data directory.
     * @param dataDirectory Directory for storing application data
     * @return Configured SpindService instance
     */
    public static SpindService createSpindService(Path dataDirectory) {
        ServerRepository serverRepository = new FileServerRepository(dataDirectory);
        CryptographyService cryptographyService = new StandardCryptographyService();
        SpindHttpService httpService = new OkHttpSpindService();
        
        return new SpindService(serverRepository, cryptographyService, httpService);
    }
    
    /**
     * Determines the appropriate data directory based on the operating system.
     * @return Path to the data directory
     */
    private static Path getDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return Path.of(System.getenv("APPDATA"), "Spind");
        } else if (os.contains("nix") || os.contains("nux")) {
            return Path.of(System.getProperty("user.home"), ".spind");
        } else if (os.contains("mac")) {
            return Path.of(System.getProperty("user.home"), "Library", "Application Support", "Spind");
        } else {
            // Fallback to user home directory
            return Path.of(System.getProperty("user.home"), ".spind");
        }
    }
    
    /**
     * Resets the singleton instance. Useful for testing.
     */
    public static void resetInstance() {
        synchronized (SpindServiceFactory.class) {
            instance = null;
        }
    }
}