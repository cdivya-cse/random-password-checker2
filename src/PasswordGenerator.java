import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * PasswordGenerator.java
 * Demonstrates:
 * 1. Cryptographic Security - Uses java.security.SecureRandom (CSPRNG) instead of Math.random().
 * 2. Algorithmic Rigor - Fisher-Yates shuffle for unbiased uniform character distribution.
 * 3. Guaranteed Inclusivity - Ensures at least one character from each selected category is present.
 * 4. Input Validation & Encapsulation.
 */
public class PasswordGenerator {

    // Cryptographically Strong Pseudo-Random Number Generator (CSPRNG)
    private final SecureRandom secureRandom;

    // Character pools
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT_CHARS     = "0123456789";
    private static final String SPECIAL_CHARS   = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    public PasswordGenerator() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a secure random password according to the specified criteria.
     * 
     * @param length Desired length (8 to 64)
     * @param useUpper Include uppercase letters (A-Z)
     * @param useLower Include lowercase letters (a-z)
     * @param useDigits Include numbers (0-9)
     * @param useSpecial Include special characters (!@#$%...)
     * @return Generated cryptographically secure password string
     */
    public String generatePassword(int length, boolean useUpper, boolean useLower, boolean useDigits, boolean useSpecial) {
        // Enforce valid bounds
        if (length < 8) length = 8;
        if (length > 64) length = 64;

        // Fallback if user unchecks all boxes
        if (!useUpper && !useLower && !useDigits && !useSpecial) {
            useLower = true;
            useDigits = true;
        }

        StringBuilder combinedPool = new StringBuilder();
        List<Character> guaranteedChars = new ArrayList<>();

        if (useUpper) {
            combinedPool.append(UPPERCASE_CHARS);
            guaranteedChars.add(getRandomChar(UPPERCASE_CHARS));
        }
        if (useLower) {
            combinedPool.append(LOWERCASE_CHARS);
            guaranteedChars.add(getRandomChar(LOWERCASE_CHARS));
        }
        if (useDigits) {
            combinedPool.append(DIGIT_CHARS);
            guaranteedChars.add(getRandomChar(DIGIT_CHARS));
        }
        if (useSpecial) {
            combinedPool.append(SPECIAL_CHARS);
            guaranteedChars.add(getRandomChar(SPECIAL_CHARS));
        }

        String pool = combinedPool.toString();
        List<Character> passwordChars = new ArrayList<>(guaranteedChars);

        // Fill remaining slots from the combined pool
        int remainingLength = length - passwordChars.size();
        for (int i = 0; i < remainingLength; i++) {
            passwordChars.add(getRandomChar(pool));
        }

        // Fisher-Yates Shuffle using SecureRandom to eliminate any predictable order
        shuffleCharacters(passwordChars);

        // Build final string
        StringBuilder finalPassword = new StringBuilder(passwordChars.size());
        for (char c : passwordChars) {
            finalPassword.append(c);
        }

        return finalPassword.toString();
    }

    /**
     * Selects a single random character from a given character set using SecureRandom.
     */
    private char getRandomChar(String charSet) {
        int index = this.secureRandom.nextInt(charSet.length());
        return charSet.charAt(index);
    }

    /**
     * In-place Fisher-Yates unbiased shuffle.
     */
    private void shuffleCharacters(List<Character> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = this.secureRandom.nextInt(i + 1);
            Character temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}
