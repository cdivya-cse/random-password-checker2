/**
 * DigitRule.java
 * Demonstrates Inheritance & Polymorphism.
 * Validates presence of at least one numeric digit (0-9).
 */
public class DigitRule extends AbstractPatternRule {
    public DigitRule() {
        super(
            "Contains numeric digit (0-9)",
            ".*[0-9].*",
            "Add at least one numeric digit (0-9).",
            20
        );
    }
}
