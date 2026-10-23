/**
 * AbstractPatternRule.java
 * Demonstrates:
 * 1. Abstraction (abstract base class defining common pattern-matching behavior)
 * 2. Inheritance (implements PasswordRule and serves as parent for specific character rules)
 */
public abstract class AbstractPatternRule implements PasswordRule {
    protected final String ruleName;
    protected final String patternRegex;
    protected final String suggestion;
    protected final int weight;

    public AbstractPatternRule(String ruleName, String patternRegex, String suggestion, int weight) {
        this.ruleName = ruleName;
        this.patternRegex = patternRegex;
        this.suggestion = suggestion;
        this.weight = weight;
    }

    @Override
    public String getName() {
        return this.ruleName;
    }

    @Override
    public boolean evaluate(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.matches(this.patternRegex);
    }

    @Override
    public String getSuggestion() {
        return this.suggestion;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }
}
