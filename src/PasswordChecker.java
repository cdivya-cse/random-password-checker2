import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * PasswordChecker.java
 * Demonstrates:
 * 1. Abstraction - Hides internal scoring & entropy algorithms behind a clean analyze() method.
 * 2. Polymorphism - Iterates over an extensible List of PasswordRule objects, invoking evaluate() dynamically.
 * 3. Defensive Programming - Input sanitization, heuristic checks, pattern penalties.
 */
public class PasswordChecker {

    // Polymorphic collection of rules
    private final List<PasswordRule> rules;

    // Common weak patterns for heuristic detection
    private static final String[] COMMON_PATTERNS = {
        "password", "123456", "12345678", "qwerty", "admin", "welcome",
        "abc123", "letmein", "monkey", "dragon", "master", "football"
    };

    public PasswordChecker() {
        this.rules = new ArrayList<>();
        // Register default rules (Polymorphic composition)
        this.rules.add(new LengthRule(8));
        this.rules.add(new UppercaseRule());
        this.rules.add(new LowercaseRule());
        this.rules.add(new DigitRule());
        this.rules.add(new SpecialCharRule());
    }

    /**
     * Allows adding custom polymorphic rules (Open/Closed Principle)
     */
    public void addRule(PasswordRule rule) {
        if (rule != null) {
            this.rules.add(rule);
        }
    }

    /**
     * Core analysis method analyzing strength, computing Shannon entropy,
     * evaluating polymorphic rules, and generating actionable suggestions.
     * 
     * @param password The candidate password
     * @return Fully populated PasswordResult object
     */
    public PasswordResult analyze(String password) {
        if (password == null || password.isEmpty()) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("Password cannot be empty.");
            suggestions.add("Use at least 8 characters with a mix of letters, numbers, and symbols.");
            return new PasswordResult(
                0, "Very Weak", 0, false, false, false, false,
                0.0, "Instant", "#ef4444", new ArrayList<>(), suggestions
            );
        }

        int length = password.length();
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        List<String> requirementsMet = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // 1. Evaluate polymorphic rules
        int ruleScore = 0;
        for (PasswordRule rule : this.rules) {
            if (rule.evaluate(password)) {
                requirementsMet.add(rule.getName());
                ruleScore += rule.getWeight();

                // Categorize specific attributes for detailed UI flags
                if (rule instanceof UppercaseRule) hasUpper = true;
                if (rule instanceof LowercaseRule) hasLower = true;
                if (rule instanceof DigitRule) hasDigit = true;
                if (rule instanceof SpecialCharRule) hasSpecial = true;
            } else {
                suggestions.add(rule.getSuggestion());
            }
        }

        // 2. Additional length bonuses
        int lengthBonus = 0;
        if (length >= 16) {
            lengthBonus = 20;
        } else if (length >= 12) {
            lengthBonus = 12;
        } else if (length >= 10) {
            lengthBonus = 6;
        }

        // 3. Character pool diversity calculation for Shannon Entropy
        int poolSize = 0;
        if (hasLower) poolSize += 26;
        if (hasUpper) poolSize += 26;
        if (hasDigit) poolSize += 10;
        if (hasSpecial) poolSize += 33; // Standard printable ASCII symbols
        if (poolSize == 0) poolSize = 1;

        // Shannon Entropy: H = L * log2(R)
        double entropyBits = length * (Math.log(poolSize) / Math.log(2.0));

        // 4. Character variety bonus (unique characters)
        Set<Character> uniqueChars = new HashSet<>();
        for (char c : password.toCharArray()) {
            uniqueChars.add(c);
        }
        int uniqueRatio = (int) (((double) uniqueChars.size() / length) * 10); // 0 to 10 points

        // 5. Penalties for common vulnerabilities
        int penalties = 0;
        String lowerPass = password.toLowerCase();

        // Penalty A: Common dictionary patterns
        for (String common : COMMON_PATTERNS) {
            if (lowerPass.contains(common)) {
                penalties += 25;
                suggestions.add("Avoid dictionary words or common phrases like '" + common + "'.");
                break;
            }
        }

        // Penalty B: Excessive repetitive characters (e.g. "aaaa" or "1111")
        if (password.matches(".*(.)\\1{2,}.*")) {
            penalties += 15;
            suggestions.add("Avoid repeating consecutive characters (e.g. 'aaa' or '111').");
        }

        // Penalty C: Simple sequential keyboard patterns (e.g. "1234", "abcd")
        if (containsSequentialChars(lowerPass)) {
            penalties += 15;
            suggestions.add("Avoid sequential character series (e.g. '1234' or 'abcd').");
        }

        // Penalty D: Short password penalty
        if (length < 8) {
            penalties += 30;
        }

        // 6. Final Score Calculation (clamped between 0 and 100)
        int rawScore = (int) ((ruleScore * 0.65) + lengthBonus + uniqueRatio - penalties);
        
        // Normalize with entropy weighting
        if (entropyBits >= 80) rawScore += 10;
        else if (entropyBits < 40) rawScore -= 10;

        int finalScore = Math.max(5, Math.min(100, rawScore));
        if (length < 6) finalScore = Math.min(20, finalScore);

        // 7. Determine Strength Level & UI Color
        String strengthLevel;
        String colorCode;

        if (finalScore >= 90) {
            strengthLevel = "Very Strong";
            colorCode = "#10b981"; // Emerald green
        } else if (finalScore >= 75) {
            strengthLevel = "Strong";
            colorCode = "#06b6d4"; // Cyan
        } else if (finalScore >= 55) {
            strengthLevel = "Medium";
            colorCode = "#f59e0b"; // Amber/yellow
        } else if (finalScore >= 35) {
            strengthLevel = "Weak";
            colorCode = "#f97316"; // Orange
        } else {
            strengthLevel = "Very Weak";
            colorCode = "#ef4444"; // Red
        }

        // 8. Crack Time Estimation (Heuristic at 100 Billion guesses/sec brute force)
        String crackTime = calculateCrackTime(entropyBits);

        // Positive feedback if password is very good
        if (suggestions.isEmpty()) {
            suggestions.add("Excellent! This password is very robust and resilient to brute-force attacks.");
        }

        return new PasswordResult(
            finalScore, strengthLevel, length,
            hasUpper, hasLower, hasDigit, hasSpecial,
            entropyBits, crackTime, colorCode,
            requirementsMet, suggestions
        );
    }

    /**
     * Checks for ascending sequences of 3+ consecutive characters (e.g. abc, 123)
     */
    private boolean containsSequentialChars(String text) {
        if (text == null || text.length() < 3) return false;
        for (int i = 0; i < text.length() - 2; i++) {
            char c1 = text.charAt(i);
            char c2 = text.charAt(i + 1);
            char c3 = text.charAt(i + 2);
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes human-friendly crack time estimate based on entropy bits.
     */
    private String calculateCrackTime(double entropyBits) {
        if (entropyBits < 28) return "Instant (< 1 second)";
        if (entropyBits < 36) return "A few seconds";
        if (entropyBits < 45) return "Several minutes";
        if (entropyBits < 55) return "Few hours to days";
        if (entropyBits < 65) return "Several months";
        if (entropyBits < 78) return "Decades to Centuries";
        return "Millions of years";
    }
}
