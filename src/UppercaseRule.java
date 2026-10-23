/**
 * UppercaseRule.java
 * Demonstrates Inheritance & Polymorphism.
 * Validates presence of at least one uppercase letter (A-Z).
 */
public class UppercaseRule extends AbstractPatternRule {
    public UppercaseRule() {
        super(
            "Contains uppercase letter (A-Z)",
            ".*[A-Z].*",
            "Add at least one uppercase letter (A-Z).",
            20
        );
    }
}
