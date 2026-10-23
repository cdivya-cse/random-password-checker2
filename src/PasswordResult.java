import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PasswordResult.java
 * Demonstrates Encapsulation in Java OOP.
 * 
 * Holds evaluation results with private state, public getters,
 * and controlled immutability.
 * 
 * Note: For security, the raw candidate password is NEVER stored or returned.
 */
public class PasswordResult {
    // Encapsulated private attributes
    private int score;                     // 0 to 100
    private String strengthLevel;          // Very Weak, Weak, Medium, Strong, Very Strong
    private int length;                    // Character count
    private boolean hasUppercase;
    private boolean hasLowercase;
    private boolean hasDigit;
    private boolean hasSpecial;
    private double entropyBits;            // Shannon information entropy in bits
    private String crackTimeEstimate;      // Human-readable brute force estimate
    private String colorCode;              // UI status color (hex)
    private List<String> requirementsMet;  // Satisfied rule labels
    private List<String> suggestions;      // Constructive improvement guidance

    // Default Constructor
    public PasswordResult() {
        this.score = 0;
        this.strengthLevel = "Very Weak";
        this.length = 0;
        this.hasUppercase = false;
        this.hasLowercase = false;
        this.hasDigit = false;
        this.hasSpecial = false;
        this.entropyBits = 0.0;
        this.crackTimeEstimate = "Instant";
        this.colorCode = "#ef4444";
        this.requirementsMet = new ArrayList<>();
        this.suggestions = new ArrayList<>();
    }

    // Parameterized Constructor
    public PasswordResult(int score, String strengthLevel, int length,
                          boolean hasUppercase, boolean hasLowercase,
                          boolean hasDigit, boolean hasSpecial,
                          double entropyBits, String crackTimeEstimate,
                          String colorCode, List<String> requirementsMet,
                          List<String> suggestions) {
        this.score = Math.max(0, Math.min(100, score));
        this.strengthLevel = strengthLevel;
        this.length = length;
        this.hasUppercase = hasUppercase;
        this.hasLowercase = hasLowercase;
        this.hasDigit = hasDigit;
        this.hasSpecial = hasSpecial;
        this.entropyBits = Math.round(entropyBits * 10.0) / 10.0;
        this.crackTimeEstimate = crackTimeEstimate;
        this.colorCode = colorCode;
        this.requirementsMet = requirementsMet != null ? new ArrayList<>(requirementsMet) : new ArrayList<>();
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
    }

    // Getters and Setters (Encapsulation)
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
    }

    public String getStrengthLevel() {
        return strengthLevel;
    }

    public void setStrengthLevel(String strengthLevel) {
        this.strengthLevel = strengthLevel;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isHasUppercase() {
        return hasUppercase;
    }

    public void setHasUppercase(boolean hasUppercase) {
        this.hasUppercase = hasUppercase;
    }

    public boolean isHasLowercase() {
        return hasLowercase;
    }

    public void setHasLowercase(boolean hasLowercase) {
        this.hasLowercase = hasLowercase;
    }

    public boolean isHasDigit() {
        return hasDigit;
    }

    public void setHasDigit(boolean hasDigit) {
        this.hasDigit = hasDigit;
    }

    public boolean isHasSpecial() {
        return hasSpecial;
    }

    public void setHasSpecial(boolean hasSpecial) {
        this.hasSpecial = hasSpecial;
    }

    public double getEntropyBits() {
        return entropyBits;
    }

    public void setEntropyBits(double entropyBits) {
        this.entropyBits = Math.round(entropyBits * 10.0) / 10.0;
    }

    public String getCrackTimeEstimate() {
        return crackTimeEstimate;
    }

    public void setCrackTimeEstimate(String crackTimeEstimate) {
        this.crackTimeEstimate = crackTimeEstimate;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public List<String> getRequirementsMet() {
        return Collections.unmodifiableList(requirementsMet);
    }

    public void addRequirementMet(String req) {
        this.requirementsMet.add(req);
    }

    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public void addSuggestion(String suggestion) {
        this.suggestions.add(suggestion);
    }

    /**
     * Converts this PasswordResult into a valid JSON string without external libraries.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"score\":").append(score).append(",");
        sb.append("\"strengthLevel\":\"").append(escapeJson(strengthLevel)).append("\",");
        sb.append("\"length\":").append(length).append(",");
        sb.append("\"hasUppercase\":").append(hasUppercase).append(",");
        sb.append("\"hasLowercase\":").append(hasLowercase).append(",");
        sb.append("\"hasDigit\":").append(hasDigit).append(",");
        sb.append("\"hasSpecial\":").append(hasSpecial).append(",");
        sb.append("\"entropyBits\":").append(entropyBits).append(",");
        sb.append("\"crackTimeEstimate\":\"").append(escapeJson(crackTimeEstimate)).append("\",");
        sb.append("\"colorCode\":\"").append(escapeJson(colorCode)).append("\",");

        // requirementsMet array
        sb.append("\"requirementsMet\":[");
        for (int i = 0; i < requirementsMet.size(); i++) {
            sb.append("\"").append(escapeJson(requirementsMet.get(i))).append("\"");
            if (i < requirementsMet.size() - 1) sb.append(",");
        }
        sb.append("],");

        // suggestions array
        sb.append("\"suggestions\":[");
        for (int i = 0; i < suggestions.size(); i++) {
            sb.append("\"").append(escapeJson(suggestions.get(i))).append("\"");
            if (i < suggestions.size() - 1) sb.append(",");
        }
        sb.append("]");

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
