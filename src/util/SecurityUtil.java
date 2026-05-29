package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Security utilities for simple hashing (course-level, SHA-256).
 */
public class SecurityUtil {
    public static String hashSHA256(String input) {
        if (input == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static boolean looksLikeSHA256Hex(String s) {
        return s != null && s.matches("[0-9a-fA-F]{64}");
    }
}

