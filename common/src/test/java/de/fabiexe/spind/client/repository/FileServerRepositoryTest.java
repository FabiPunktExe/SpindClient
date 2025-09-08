package de.fabiexe.spind.client.repository;

import de.fabiexe.spind.client.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileServerRepository.
 */
class FileServerRepositoryTest {
    
    @TempDir
    Path tempDir;
    
    private FileServerRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new FileServerRepository(tempDir);
    }
    
    @Test
    void testGetServersWhenFileDoesNotExist() {
        List<Server> servers = repository.getServers();
        assertNotNull(servers);
        assertTrue(servers.isEmpty());
    }
    
    @Test
    void testSetAndGetServers() {
        List<Server> originalServers = List.of(
            new Server("Test Server 1", "https://spind1.example.com", "user1"),
            new Server("Test Server 2", "https://spind2.example.com", "user2")
        );
        
        // Save servers
        boolean saveResult = repository.setServers(originalServers);
        assertTrue(saveResult);
        
        // Retrieve servers
        List<Server> retrievedServers = repository.getServers();
        assertNotNull(retrievedServers);
        assertEquals(originalServers.size(), retrievedServers.size());
        
        // Verify content matches
        for (int i = 0; i < originalServers.size(); i++) {
            Server original = originalServers.get(i);
            Server retrieved = retrievedServers.get(i);
            assertEquals(original.getName(), retrieved.getName());
            assertEquals(original.getAddress(), retrieved.getAddress());
            assertEquals(original.getUsername(), retrieved.getUsername());
        }
    }
    
    @Test
    void testEmptyServersList() {
        List<Server> emptyList = List.of();
        
        boolean saveResult = repository.setServers(emptyList);
        assertTrue(saveResult);
        
        List<Server> retrievedServers = repository.getServers();
        assertNotNull(retrievedServers);
        assertTrue(retrievedServers.isEmpty());
    }
}