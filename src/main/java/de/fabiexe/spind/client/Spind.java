package de.fabiexe.spind.client;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Spind {
    private static final int SAFE_VERSION = 1;
    private static final Path directory;
    private static final Gson gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    private static final List<UnlockedSafe> unlockedSafes = new ArrayList<>();

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            directory = Path.of(System.getenv("APPDATA"), "Spind");
        } else if (os.contains("nix") || os.contains("nux")) {
            directory = Path.of(System.getProperty("user.home"), ".spind");
        } else if (os.contains("mac")) {
            directory = Path.of(System.getProperty("user.home"), "Library", "Application Support", "Spind");
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }
    }

    public static List<Server> getServers(JsonArray params) {
        try {
            String json = Files.readString(directory.resolve("servers.json"));
            return gson.fromJson(json, new TypeToken<>() {});
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return List.of();
        }
    }

    public static Object setServers(JsonArray params) {
        try {
            List<Server> servers = gson.fromJson(params.get(0), new TypeToken<>() {});
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("servers.json"), gson.toJson(servers));
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    public static boolean isLocked(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            for (UnlockedSafe safe : unlockedSafes) {
                if (safe.server.equals(server)) {
                    return false;
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
        }
        return true;
    }

    public static Object unlock(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            for (UnlockedSafe safe : unlockedSafes) {
                if (safe.server.equals(server)) {
                    return true; // Already unlocked
                }
            }
            String password = gson.fromJson(params.get(1), String.class);
            if (password == null || password.isEmpty()) {
                return false;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String passwordHash = new String(digest.digest(password.getBytes()));
            String secret = new String(digest.digest(passwordHash.getBytes()));

            String authorization = Base64.getEncoder().encodeToString((server.username() + ":" + secret).getBytes());
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.address() + "/v1/passwords"))
                    .GET()
                    .header("Authorization", "Basic " + authorization)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            client.close();

            if (response.statusCode() == 404 || response.statusCode() == 405) {
                return "The server does not support your Spind version";
            } else if (response.statusCode() == 412) {
                return false;
            } else if (response.statusCode() != 200) {
                return new String(response.body());
            }

            byte[] bytes = response.body();
            List<Password> passwords = readSafe(passwordHash, bytes);
            if (passwords == null) {
                return "Invalid password or corrupted safe";
            }

            unlockedSafes.add(new UnlockedSafe(server, passwordHash, secret, passwords));

            return true;
        } catch (JsonSyntaxException | IndexOutOfBoundsException | NoSuchAlgorithmException | IOException |
                 InterruptedException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | IllegalArgumentException e) {
            e.printStackTrace(System.err);
            return e.getMessage();
        }
    }

    public static Object lock(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            unlockedSafes.removeIf(safe -> safe.server.equals(server));
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    public static Object setup(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            String password = gson.fromJson(params.get(1), String.class);
            if (server == null || password == null || password.isEmpty()) {
                return "Invalid parameters";
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String passwordHash = new String(digest.digest(password.getBytes()));
            String secret = new String(digest.digest(passwordHash.getBytes()));

            byte[] bytes = writeSafe(passwordHash, List.of());

            String authorization = Base64.getEncoder().encodeToString((server.username() + ":" + secret).getBytes());
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.address() + "/v1/passwords"))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .header("Authorization", "Basic " + authorization)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            client.close();

            if (response.statusCode() == 404 || response.statusCode() == 405) {
                return "The server does not support your Spind version";
            } else if (response.statusCode() != 200) {
                return response.body();
            }

            return true;
        } catch (JsonSyntaxException | NoSuchAlgorithmException | IOException | InterruptedException |
                 NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace(System.err);
            return e.getMessage();
        }
    }

    public static List<Password> getPasswords(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            for (UnlockedSafe safe : unlockedSafes) {
                if (safe.server.equals(server)) {
                    return safe.passwords;
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
        }
        return List.of();
    }

    public static Object setPasswords(JsonArray params) {
        try {
            Server server = gson.fromJson(params.get(0), Server.class);
            List<Password> passwords = gson.fromJson(params.get(1), new TypeToken<List<Password>>() {}.getType());

            UnlockedSafe unlockedSafe = null;
            for (UnlockedSafe unlockedSafe2 : unlockedSafes) {
                if (unlockedSafe2.server.equals(server)) {
                    unlockedSafe = unlockedSafe2;
                }
            }
            if (unlockedSafe == null) {
                return "Safe is not unlocked";
            }

            byte[] bytes = writeSafe(unlockedSafe.passwordHash, passwords);

            String authorization = Base64.getEncoder().encodeToString((server.username() + ":" + unlockedSafe.secret).getBytes());
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.address() + "/v1/passwords"))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .header("Authorization", "Basic " + authorization)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            client.close();

            if (response.statusCode() == 404 || response.statusCode() == 405) {
                return "The server does not support your Spind version";
            } else if (response.statusCode() != 200) {
                return response.body();
            }

            unlockedSafe.passwords.clear();
            unlockedSafe.passwords.addAll(passwords);
            return true;
        } catch (JsonSyntaxException | IOException | InterruptedException | NoSuchAlgorithmException |
                 NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace(System.err);
            return e.getMessage();
        }
    }

    public static Object copyToClipboard(JsonArray params) {
        try {
            String text = params.get(0).getAsString();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (JsonSyntaxException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    private static byte @NotNull [] writeSafe(@NotNull String passwordHash, @NotNull List<Password> passwords) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ByteBuffer versionBuffer = ByteBuffer.allocate(4);
        versionBuffer.putInt(SAFE_VERSION);
        byte[] versionBytes = versionBuffer.array();

        byte[] passwordsBytes = gson.toJson(passwords).getBytes();
        byte[] key = new byte[32];
        System.arraycopy(passwordHash.getBytes(), 0, key, 0, key.length);
        SecretKey secret = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        passwordsBytes = cipher.doFinal(passwordsBytes);

        byte[] bytes = new byte[versionBytes.length + passwordsBytes.length];
        System.arraycopy(versionBytes, 0, bytes, 0, versionBytes.length);
        System.arraycopy(passwordsBytes, 0, bytes, versionBytes.length, passwordsBytes.length);
        return bytes;
    }

    private static @Nullable List<Password> readSafe(@NotNull String passwordHash, byte @NotNull [] bytes) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (bytes.length < 4) {
            throw new IllegalArgumentException("Corrupted data");
        }

        ByteBuffer versionBuffer = ByteBuffer.wrap(bytes, 0, 4);
        int version = versionBuffer.getInt();
        if (version != 1) {
            if (SAFE_VERSION < version) {
                throw new IllegalArgumentException("Your Spind version is too old to unlock this safe, please update Spind");
            } else {
                throw new IllegalArgumentException("Your Spind version is too new to unlock this safe, please downgrade Spind");
            }
        }

        byte[] passwordsBytes = new byte[bytes.length - 4];
        System.arraycopy(bytes, 4, passwordsBytes, 0, passwordsBytes.length);
        byte[] key = new byte[32];
        System.arraycopy(passwordHash.getBytes(), 0, key, 0, key.length);
        SecretKey secret = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        passwordsBytes = cipher.doFinal(passwordsBytes);

        return gson.fromJson(new String(passwordsBytes), new TypeToken<List<Password>>() {}.getType());
    }

    private record UnlockedSafe(Server server, String passwordHash, String secret, List<Password> passwords) {}
}
