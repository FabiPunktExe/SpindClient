package de.fabiexe.spind.client.service;

import de.fabiexe.spind.client.Server;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Base64;

/**
 * OkHttp-based implementation of SpindHttpService.
 */
public class OkHttpSpindService implements SpindHttpService {
    
    private final OkHttpClient httpClient = new OkHttpClient();
    
    @Override
    public byte @NotNull [] downloadSafe(@NotNull Server server, @NotNull String secret) throws SpindHttpException {
        String authorization = Base64.getEncoder().encodeToString((server.getUsername() + ":" + secret).getBytes());
        Request request = new Request.Builder()
                .get()
                .url(server.getAddress() + "/v1/passwords")
                .header("Authorization", "Basic " + authorization)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            return handleResponse(response);
        } catch (IOException e) {
            throw new SpindHttpException("Network error during download", e);
        }
    }
    
    @Override
    public void uploadSafe(@NotNull Server server, @NotNull String secret, byte @NotNull [] safeData) throws SpindHttpException {
        String authorization = Base64.getEncoder().encodeToString((server.getUsername() + ":" + secret).getBytes());
        Request request = new Request.Builder()
                .post(RequestBody.create(safeData))
                .url(server.getAddress() + "/v1/passwords")
                .header("Authorization", "Basic " + authorization)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            handleResponse(response);
        } catch (IOException e) {
            throw new SpindHttpException("Network error during upload", e);
        }
    }
    
    private byte[] handleResponse(Response response) throws SpindHttpException, IOException {
        switch (response.code()) {
            case 200:
                if (response.body() == null) {
                    throw new SpindHttpException("Empty response body", response.code());
                }
                return response.body().bytes();
            case 404:
            case 405:
                throw new SpindHttpException("The server does not support your Spind version", response.code());
            case 412:
                throw new SpindHttpException("Invalid password", response.code());
            default:
                String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                throw new SpindHttpException("Server error: " + errorMessage, response.code());
        }
    }
}