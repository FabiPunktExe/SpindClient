package de.fabiexe.spind.client.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.google.gson.reflect.TypeToken;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import de.fabiexe.spind.client.Password;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard implementation of CryptographyService using AES encryption.
 * Supports multiple safe format versions for backward compatibility.
 */
public class StandardCryptographyService implements CryptographyService {
    
    private static final int CURRENT_SAFE_VERSION = 3;
    private static final Gson gson = new GsonBuilder().setStrictness(Strictness.LENIENT).create();
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    
    @Override
    public byte @NotNull [] writeSafe(@NotNull String passwordHash, @NotNull List<Password> passwords) 
            throws CryptographyException {
        try {
            ByteBuffer versionBuffer = ByteBuffer.allocate(4);
            versionBuffer.putInt(CURRENT_SAFE_VERSION);
            byte[] versionBytes = versionBuffer.array();

            byte[] passwordsBytes = gson.toJson(passwords).getBytes();
            byte[] encryptedPasswords = encryptData(passwordHash, passwordsBytes);

            byte[] result = new byte[versionBytes.length + encryptedPasswords.length];
            System.arraycopy(versionBytes, 0, result, 0, versionBytes.length);
            System.arraycopy(encryptedPasswords, 0, result, versionBytes.length, encryptedPasswords.length);
            return result;
        } catch (Exception e) {
            throw new CryptographyException("Failed to write safe", e);
        }
    }
    
    @Override
    public @Nullable List<Password> readSafe(@NotNull String passwordHash, byte @NotNull [] safeData) 
            throws CryptographyException {
        try {
            if (safeData.length < 4) {
                throw new CryptographyException("Corrupted safe data");
            }

            ByteBuffer versionBuffer = ByteBuffer.wrap(safeData, 0, 4);
            int version = versionBuffer.getInt();
            
            return switch (version) {
                case 1 -> readSafeV1(passwordHash, safeData);
                case 2 -> readSafeV2(passwordHash, safeData);
                case 3 -> readSafeV3(passwordHash, safeData);
                default -> {
                    if (CURRENT_SAFE_VERSION < version) {
                        throw new CryptographyException("Safe version " + version + " is too new. Please update your Spind client.");
                    } else {
                        throw new CryptographyException("Safe version " + version + " is no longer supported.");
                    }
                }
            };
        } catch (CryptographyException e) {
            throw e;
        } catch (Exception e) {
            throw new CryptographyException("Failed to read safe", e);
        }
    }
    
    @Override
    public @NotNull String hashPassword(@NotNull String input) throws CryptographyException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new String(digest.digest(input.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new CryptographyException("SHA-256 algorithm not available", e);
        }
    }
    
    @Override
    public int generate2FACode(@NotNull String secret) {
        return googleAuthenticator.getTotpPassword(secret);
    }
    
    private byte[] encryptData(String passwordHash, byte[] data) throws NoSuchPaddingException, 
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] key = new byte[32];
        System.arraycopy(passwordHash.getBytes(), 0, key, 0, Math.min(key.length, passwordHash.getBytes().length));
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
    
    private byte[] decryptData(String passwordHash, byte[] encryptedData) throws NoSuchPaddingException, 
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] key = new byte[32];
        System.arraycopy(passwordHash.getBytes(), 0, key, 0, Math.min(key.length, passwordHash.getBytes().length));
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }
    
    private @Nullable List<Password> readSafeV1(@NotNull String passwordHash, byte @NotNull [] bytes) 
            throws CryptographyException {
        try {
            byte[] encryptedData = extractPasswordData(bytes);
            byte[] decryptedData = decryptData(passwordHash, encryptedData);
            
            List<Password.V1> v1Passwords = gson.fromJson(new String(decryptedData), 
                new TypeToken<List<Password.V1>>() {}.getType());
            if (v1Passwords == null) {
                return null;
            }
            
            return migrateFromV1(v1Passwords);
        } catch (Exception e) {
            throw new CryptographyException("Failed to read safe v1", e);
        }
    }
    
    private @Nullable List<Password> readSafeV2(@NotNull String passwordHash, byte @NotNull [] bytes) 
            throws CryptographyException {
        try {
            byte[] encryptedData = extractPasswordData(bytes);
            byte[] decryptedData = decryptData(passwordHash, encryptedData);
            
            List<Password.V2> v2Passwords = gson.fromJson(new String(decryptedData), 
                new TypeToken<List<Password.V2>>() {}.getType());
            if (v2Passwords == null) {
                return null;
            }
            
            return migrateFromV2(v2Passwords);
        } catch (Exception e) {
            throw new CryptographyException("Failed to read safe v2", e);
        }
    }
    
    private @Nullable List<Password> readSafeV3(@NotNull String passwordHash, byte @NotNull [] bytes) 
            throws CryptographyException {
        try {
            byte[] encryptedData = extractPasswordData(bytes);
            byte[] decryptedData = decryptData(passwordHash, encryptedData);
            
            return gson.fromJson(new String(decryptedData), new TypeToken<List<Password>>() {}.getType());
        } catch (Exception e) {
            throw new CryptographyException("Failed to read safe v3", e);
        }
    }
    
    private byte[] extractPasswordData(byte[] safeData) {
        byte[] passwordsBytes = new byte[safeData.length - 4];
        System.arraycopy(safeData, 4, passwordsBytes, 0, passwordsBytes.length);
        return passwordsBytes;
    }
    
    private List<Password> migrateFromV1(List<Password.V1> v1Passwords) {
        List<Password> result = new ArrayList<>();
        for (Password.V1 password : v1Passwords) {
            Map<String, String> fields = new HashMap<>();
            if (password.getEmail() != null) {
                fields.put("Email", password.getEmail());
            }
            if (password.getPhone() != null) {
                fields.put("Phone", password.getPhone());
            }
            result.add(new Password(password.getName(), password.getPassword(), fields));
        }
        return result;
    }
    
    private List<Password> migrateFromV2(List<Password.V2> v2Passwords) {
        List<Password> result = new ArrayList<>();
        for (Password.V2 password : v2Passwords) {
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
            result.add(new Password(password.getName(), password.getPassword(), fields));
        }
        return result;
    }
}