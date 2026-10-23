/**
 * PasswordRule.java
 * Demonstrates Abstraction & Polymorphism in Java OOP.
 * 
 * Defines the contract that every password evaluation rule must fulfill.
 */
public interface PasswordRule {
    /**
     * @return User-friendly name of the rule (e.g., "Minimum Length")
     */
    String getName();

    /**
     * Evaluates whether the given password satisfies the rule.
     * @param password The candidate password string
     * @return true if passed, false otherwise
     */
    boolean evaluate(String password);

    /**
     * @return Recommendation / suggestion if rule is NOT satisfied
     */
    String getSuggestion();

    /**
     * @return Relative weight / points contributed towards overall score
     */
    int getWeight();
}
