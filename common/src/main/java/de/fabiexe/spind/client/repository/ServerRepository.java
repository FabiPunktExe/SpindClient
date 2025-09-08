package de.fabiexe.spind.client.repository;

import de.fabiexe.spind.client.Server;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Repository interface for Server persistence operations.
 * Abstracts away the file storage implementation.
 */
public interface ServerRepository {
    
    /**
     * Retrieves all stored servers.
     * @return List of servers, empty list if none exist
     */
    @NotNull List<Server> getServers();
    
    /**
     * Persists the list of servers.
     * @param servers List of servers to save
     * @return true if successful, false otherwise
     */
    boolean setServers(@NotNull List<Server> servers);
}