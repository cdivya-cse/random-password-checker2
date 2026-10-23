/**
 * LowercaseRule.java
 * Demonstrates Inheritance & Polymorphism.
 * Validates presence of at least one lowercase letter (a-z).
 */
public class LowercaseRule extends AbstractPatternRule {
    public LowercaseRule() {
        super(
            "Contains lowercase letter (a-z)",
            ".*[a-z].*",
            "Add at least one lowercase letter (a-z).",
            15
        );
    }
}
