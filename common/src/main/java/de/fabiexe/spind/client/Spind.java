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
    static Path directory;
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
        }
    }

    public static @NotNull List<Server> getServers() {
        if (!Files.exists(directory.resolve("servers.json"))) {
            return List.of();
        }
        try {
            String json = new String(Files.readAllBytes(directory.resolve("servers.json")));
            return gson.fromJson(json, new TypeToken<>() {});
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace(System.err);
            return List.of();
        }
    }

    public static boolean setServers(@NotNull List<Server> servers) {
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve("servers.json"), gson.toJson(servers).getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public static boolean isLocked(@NotNull Server server) {
        for (UnlockedSafe safe : unlockedSafes) {
            if (safe.server.equals(server)) {
                return false;
            }
        }
        return true;
    }

    public static boolean unlock(@NotNull Server server, @NotNull String password) {
        try {
            if (!isLocked(server)) {
                return true;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String passwordHash = new String(digest.digest(password.getBytes()));
            String secret = new String(digest.digest(passwordHash.getBytes()));

            String authorization = Base64.getEncoder().encodeToString((server.getUsername() + ":" + secret).getBytes());
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.getAddress() + "/v1/passwords"))
                    .GET()
                    .header("Authorization", "Basic " + authorization)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            client.close();

            if (response.statusCode() == 404 || response.statusCode() == 405) {
                throw new RuntimeException("The server does not support your Spind version");
            } else if (response.statusCode() == 412) {
                return false;
            } else if (response.statusCode() != 200) {
                throw new RuntimeException(new String(response.body()));
            }

            byte[] bytes = response.body();
            List<Password> passwords = readSafe(passwordHash, bytes);
            if (passwords == null) {
                throw new RuntimeException("Invalid password or corrupted safe");
            }

            unlockedSafes.add(new UnlockedSafe(server, passwordHash, secret, passwords));

            return true;
        } catch (JsonSyntaxException | IndexOutOfBoundsException | NoSuchAlgorithmException | IOException |
                 InterruptedException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | IllegalArgumentException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void lock(@NotNull Server server) {
        unlockedSafes.removeIf(safe -> safe.server.equals(server));
    }

    public static void setup(@NotNull Server server, @NotNull String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String passwordHash = new String(digest.digest(password.getBytes()));
            String secret = new String(digest.digest(passwordHash.getBytes()));

            byte[] bytes = writeSafe(passwordHash, List.of());

            String authorization = Base64.getEncoder().encodeToString((server.getUsername() + ":" + secret).getBytes());
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.getAddress() + "/v1/passwords"))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .header("Authorization", "Basic " + authorization)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            client.close();

            if (response.statusCode() == 404 || response.statusCode() == 405) {
                throw new RuntimeException("The server does not support your Spind version");
            } else if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }
        } catch (JsonSyntaxException | NoSuchAlgorithmException | IOException | InterruptedException |
                 NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static @NotNull List<Password> getPasswords(@NotNull Server server) {
        for (UnlockedSafe safe : unlockedSafes) {
            if (safe.server.equals(server)) {
                return safe.passwords;
            }
        }
        return List.of();
    }

    public static boolean setPasswords(@NotNull Server server, @NotNull List<Password> passwords) {
        try {
            UnlockedSafe unlockedSafe = null;
            for (UnlockedSafe unlockedSafe2 : unlockedSafes) {
                if (unlockedSafe2.server.equals(server)) {
                    unlockedSafe = unlockedSafe2;
                }
            }
            if (unlockedSafe == null) {
                throw new RuntimeException("Safe is not unlocked");
            }

            byte[] bytes = writeSafe(unlockedSafe.passwordHash, passwords);

            String authorization = Base64.getEncoder().encodeToString((server.getUsername() + ":" + unlockedSafe.secret).getBytes());
            HttpRequest request = HttpRequest.newBuilder(URI.create(server.getAddress() + "/v1/passwords"))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                    .header("Authorization", "Basic " + authorization)
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            client.close();

            if (response.statusCode() == 404 || response.statusCode() == 405) {
                throw new RuntimeException("The server does not support your Spind version");
            } else if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }

            unlockedSafe.passwords.clear();
            unlockedSafe.passwords.addAll(passwords);
            return true;
        } catch (JsonSyntaxException | IOException | InterruptedException | NoSuchAlgorithmException |
                 NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void copyToClipboard(@NotNull String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
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

    private static class UnlockedSafe {
        final Server server;
        final String passwordHash;
        final String secret;
        final List<Password> passwords;

        UnlockedSafe(Server server, String passwordHash, String secret, List<Password> passwords) {
            this.server = server;
            this.passwordHash = passwordHash;
            this.secret = secret;
            this.passwords = passwords;
        }

        public Server server() {
            return server;
        }

        public String passwordHash() {
            return passwordHash;
        }

        public String secret() {
            return secret;
        }

        public List<Password> passwords() {
            return passwords;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnlockedSafe that = (UnlockedSafe) o;
            return server.equals(that.server) && 
                   passwordHash.equals(that.passwordHash) && 
                   secret.equals(that.secret) && 
                   passwords.equals(that.passwords);
        }

        @Override
        public int hashCode() {
            return Objects.hash(server, passwordHash, secret, passwords);
        }

        @Override
        public String toString() {
            return "UnlockedSafe[" +
                   "server=" + server + ", " +
                   "passwordHash=" + passwordHash + ", " +
                   "secret=" + secret + ", " +
                   "passwords=" + passwords + ']';
        }
    }
}
