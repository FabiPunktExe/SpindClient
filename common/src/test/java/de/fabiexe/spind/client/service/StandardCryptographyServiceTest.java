package de.fabiexe.spind.client.service;

import de.fabiexe.spind.client.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StandardCryptographyService.
 * Demonstrates the improved testability of the refactored architecture.
 */
class StandardCryptographyServiceTest {
    
    private StandardCryptographyService cryptographyService;
    
    @BeforeEach
    void setUp() {
        cryptographyService = new StandardCryptographyService();
    }
    
    @Test
    void testPasswordHashing() throws CryptographyException {
        String password = "testPassword123";
        String hash1 = cryptographyService.hashPassword(password);
        String hash2 = cryptographyService.hashPassword(password);
        
        assertNotNull(hash1);
        assertEquals(hash1, hash2); // Same input should produce same hash
        assertNotEquals(password, hash1); // Hash should be different from input
    }
    
    @Test
    void testSafeWriteAndRead() throws CryptographyException {
        String passwordHash = "testPasswordHash";
        List<Password> originalPasswords = List.of(
            new Password("Test Account", "password123", Map.of("Email", "test@example.com")),
            new Password("Another Account", "secret456", Map.of("Username", "user123"))
        );
        
        // Encrypt the passwords
        byte[] safeData = cryptographyService.writeSafe(passwordHash, originalPasswords);
        assertNotNull(safeData);
        assertTrue(safeData.length > 0);
        
        // Decrypt the passwords
        List<Password> decryptedPasswords = cryptographyService.readSafe(passwordHash, safeData);
        assertNotNull(decryptedPasswords);
        assertEquals(originalPasswords.size(), decryptedPasswords.size());
        
        // Verify the content matches
        for (int i = 0; i < originalPasswords.size(); i++) {
            Password original = originalPasswords.get(i);
            Password decrypted = decryptedPasswords.get(i);
            assertEquals(original.getName(), decrypted.getName());
            assertEquals(original.getPassword(), decrypted.getPassword());
            assertEquals(original.getFields(), decrypted.getFields());
        }
    }
    
    @Test
    void testSafeReadWithWrongPassword() throws CryptographyException {
        String correctPasswordHash = "correctPassword";
        String wrongPasswordHash = "wrongPassword";
        List<Password> passwords = List.of(
            new Password("Test", "secret", Map.of())
        );
        
        byte[] safeData = cryptographyService.writeSafe(correctPasswordHash, passwords);
        
        // Try to decrypt with wrong password - should throw exception or return null
        assertThrows(CryptographyException.class, () -> {
            cryptographyService.readSafe(wrongPasswordHash, safeData);
        });
    }
    
    @Test
    void testGenerate2FACode() {
        String secret = "JBSWY3DPEHPK3PXP"; // Base32 encoded test secret
        int code = cryptographyService.generate2FACode(secret);
        
        assertTrue(code >= 0 && code <= 999999); // 6-digit code
    }
    
    @Test
    void testSafeReadWithCorruptedData() {
        String passwordHash = "testPassword";
        byte[] corruptedData = new byte[]{1, 2, 3}; // Too short to be valid
        
        assertThrows(CryptographyException.class, () -> {
            cryptographyService.readSafe(passwordHash, corruptedData);
        });
    }
}