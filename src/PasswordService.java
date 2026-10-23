/**
 * PasswordService.java
 * Demonstrates Service Layer Architecture & Separation of Concerns.
 * 
 * Coordinates domain logic between PasswordChecker and PasswordGenerator,
 * keeping business logic completely decoupled from HTTP server/controller code.
 */
public class PasswordService {

    private final PasswordChecker checker;
    private final PasswordGenerator generator;

    public PasswordService() {
        this.checker = new PasswordChecker();
        this.generator = new PasswordGenerator();
    }

    /**
     * Checks the strength of the provided password.
     * @param password Raw candidate password
     * @return Detailed PasswordResult model
     */
    public PasswordResult checkPassword(String password) {
        return this.checker.analyze(password);
    }

    /**
     * Generates a secure random password with requested parameters.
     * @param length Character length (8-64)
     * @param useUpper Include uppercase letters
     * @param useLower Include lowercase letters
     * @param useDigits Include numeric digits
     * @param useSpecial Include symbols
     * @return Securely generated password string
     */
    public String generatePassword(int length, boolean useUpper, boolean useLower, boolean useDigits, boolean useSpecial) {
        return this.generator.generatePassword(length, useUpper, useLower, useDigits, useSpecial);
    }

    /**
     * Generates a password and automatically performs instant strength analysis on it.
     * @return JSON string containing the generated password and its full PasswordResult analysis.
     */
    public String generatePasswordWithAnalysis(int length, boolean useUpper, boolean useLower, boolean useDigits, boolean useSpecial) {
        String generated = this.generator.generatePassword(length, useUpper, useLower, useDigits, useSpecial);
        PasswordResult analysis = this.checker.analyze(generated);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"password\":\"").append(escapeJson(generated)).append("\",");
        sb.append("\"analysis\":").append(analysis.toJson());
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
