package de.fabiexe.spind.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Spind {
    private static final int SAFE_VERSION = 3;
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
            Request request = new Request.Builder()
                    .get()
                    .url(server.getAddress() + "/v1/passwords")
                    .header("Authorization", "Basic " + authorization)
                    .build();
            OkHttpClient client = new OkHttpClient();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 404 || response.code() == 405) {
                    throw new RuntimeException("The server does not support your Spind version");
                } else if (response.code() == 412) {
                    return false;
                } else if (response.code() != 200) {
                    throw new RuntimeException(response.body().string());
                } else {
                    List<Password> passwords = readSafe(passwordHash, response.body().bytes());
                    if (passwords == null) {
                        throw new RuntimeException("Invalid password or corrupted safe");
                    }
                    unlockedSafes.add(new UnlockedSafe(server, passwordHash, secret, passwords));
                }
            }

            return true;
        } catch (JsonSyntaxException | IndexOutOfBoundsException | NoSuchAlgorithmException |
                 IOException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {
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
            Request request = new Request.Builder()
                    .post(RequestBody.create(bytes))
                    .url(server.getAddress() + "/v1/passwords")
                    .header("Authorization", "Basic " + authorization)
                    .build();
            OkHttpClient client = new OkHttpClient();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 404 || response.code() == 405) {
                    throw new RuntimeException("The server does not support your Spind version");
                } else if (response.code() != 200) {
                    throw new RuntimeException(response.body().string());
                }
            }
        } catch (JsonSyntaxException | NoSuchAlgorithmException | IOException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
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
            Request request = new Request.Builder()
                    .post(RequestBody.create(bytes))
                    .url(server.getAddress() + "/v1/passwords")
                    .header("Authorization", "Basic " + authorization)
                    .build();
            OkHttpClient client = new OkHttpClient();
            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 404 || response.code() == 405) {
                    throw new RuntimeException("The server does not support your Spind version");
                } else if (response.code() != 200) {
                    throw new RuntimeException(response.body().string());
                }
                unlockedSafe.passwords.clear();
                unlockedSafe.passwords.addAll(passwords);
            }
            return true;
        } catch (JsonSyntaxException | IOException | NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage());
        }
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
        if (version == 1) {
            return readSafeV1(passwordHash, bytes);
        } else if (version == 2) {
            return readSafeV2(passwordHash, bytes);
        } else if (version == 3) {
            return readSafeV3(passwordHash, bytes);
        } else if (SAFE_VERSION < version) {
            throw new IllegalArgumentException("Your Spind version is too old to unlock this safe, please update Spind");
        } else {
            throw new IllegalArgumentException("Your Spind version is too new to unlock this safe, please downgrade Spind");
        }
    }

    private static @Nullable List<Password> readSafeV1(@NotNull String passwordHash, byte @NotNull [] bytes) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] passwordsBytes = new byte[bytes.length - 4];
        System.arraycopy(bytes, 4, passwordsBytes, 0, passwordsBytes.length);
        byte[] key = new byte[32];
        System.arraycopy(passwordHash.getBytes(), 0, key, 0, key.length);
        SecretKey secret = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        passwordsBytes = cipher.doFinal(passwordsBytes);

        List<Password.V1> v1 = gson.fromJson(new String(passwordsBytes), new TypeToken<List<Password.V1>>() {}.getType());
        if (v1 == null) {
            return null;
        }

        List<Password> v3 = new ArrayList<>();
        for (Password.V1 password : v1) {
            Map<String, String> fields = new HashMap<>();
            if (password.getEmail() != null) {
                fields.put("Email", password.getEmail());
            }
            if (password.getPhone() != null) {
                fields.put("Phone", password.getPhone());
            }
            v3.add(new Password(password.getName(), password.getPassword(), fields));
        }
        return v3;
    }

    private static @Nullable List<Password> readSafeV2(@NotNull String passwordHash, byte @NotNull [] bytes) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] passwordsBytes = new byte[bytes.length - 4];
        System.arraycopy(bytes, 4, passwordsBytes, 0, passwordsBytes.length);
        byte[] key = new byte[32];
        System.arraycopy(passwordHash.getBytes(), 0, key, 0, key.length);
        SecretKey secret = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        passwordsBytes = cipher.doFinal(passwordsBytes);

        List<Password.V2> v2 = gson.fromJson(new String(passwordsBytes), new TypeToken<List<Password.V2>>() {}.getType());
        if (v2 == null) {
            return null;
        }

        List<Password> v3 = new ArrayList<>();
        for (Password.V2 password : v2) {
            Map<String, String> fields = new HashMap<>();
            if (password.getUsername() != null) {
                fields.put("Username", password.getUsername());
            }
            if (password.getEmail() != null) {
                fields.put("Email", password.getEmail());
            }
            if (password.getPhone() != null) {
                fields.put("Phone", password.getPhone());
            }
            v3.add(new Password(password.getName(), password.getPassword(), fields));
        }
        return v3;
    }

    private static @Nullable List<Password> readSafeV3(@NotNull String passwordHash, byte @NotNull [] bytes) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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
