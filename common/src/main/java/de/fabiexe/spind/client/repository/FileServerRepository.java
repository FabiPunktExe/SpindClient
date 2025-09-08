package de.fabiexe.spind.client.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import de.fabiexe.spind.client.Server;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * File-based implementation of ServerRepository.
 * Stores servers as JSON in the file system.
 */
public class FileServerRepository implements ServerRepository {
    
    private static final Gson gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    private final Path serversFile;
    
    public FileServerRepository(Path directory) {
        this.serversFile = directory.resolve("servers.json");
    }
    
    @Override
    public @NotNull List<Server> getServers() {
        if (!Files.exists(serversFile)) {
            return List.of();
        }
        try {
            String json = new String(Files.readAllBytes(serversFile));
            return gson.fromJson(json, new TypeToken<List<Server>>() {}.getType());
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return List.of();
        }
    }
    
    @Override
    public boolean setServers(@NotNull List<Server> servers) {
        try {
            Files.createDirectories(serversFile.getParent());
            Files.write(serversFile, gson.toJson(servers).getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }
}