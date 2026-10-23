/**
 * LengthRule.java
 * Demonstrates Polymorphism and Interface Implementation.
 * Checks if the password meets the minimum length requirement.
 */
public class LengthRule implements PasswordRule {
    private final int minLength;

    public LengthRule(int minLength) {
        this.minLength = minLength;
    }

    @Override
    public String getName() {
        return "At least " + minLength + " characters";
    }

    @Override
    public boolean evaluate(String password) {
        return password != null && password.length() >= minLength;
    }

    @Override
    public String getSuggestion() {
        return "Increase password length to at least " + minLength + " characters (12+ recommended).";
    }

    @Override
    public int getWeight() {
        return 25;
    }
}
