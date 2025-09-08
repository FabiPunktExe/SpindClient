package de.fabiexe.spind.client.service;

import de.fabiexe.spind.client.Password;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Service interface for cryptographic operations on password safes.
 * Handles encryption, decryption and safe format migration.
 */
public interface CryptographyService {
    
    /**
     * Encrypts a list of passwords into a safe format.
     * @param passwordHash The hash of the master password for encryption
     * @param passwords List of passwords to encrypt
     * @return Encrypted safe data as byte array
     * @throws CryptographyException If encryption fails
     */
    byte @NotNull [] writeSafe(@NotNull String passwordHash, @NotNull List<Password> passwords) 
            throws CryptographyException;
    
    /**
     * Decrypts safe data and returns the contained passwords.
     * Automatically handles different safe format versions.
     * @param passwordHash The hash of the master password for decryption
     * @param safeData Encrypted safe data
     * @return List of decrypted passwords, null if password is invalid
     * @throws CryptographyException If decryption fails
     */
    @Nullable List<Password> readSafe(@NotNull String passwordHash, byte @NotNull [] safeData) 
            throws CryptographyException;
    
    /**
     * Generates a SHA-256 hash of the input string.
     * @param input String to hash
     * @return SHA-256 hash as string
     * @throws CryptographyException If hashing fails
     */
    @NotNull String hashPassword(@NotNull String input) throws CryptographyException;
    
    /**
     * Generates a 2FA TOTP code from a secret.
     * @param secret Base32 encoded secret
     * @return 6-digit TOTP code
     */
    int generate2FACode(@NotNull String secret);
}