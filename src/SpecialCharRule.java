/**
 * SpecialCharRule.java
 * Demonstrates Inheritance & Polymorphism.
 * Validates presence of at least one special character / symbol.
 */
public class SpecialCharRule extends AbstractPatternRule {
    public SpecialCharRule() {
        super(
            "Contains special character (!@#$%^&*...)",
            ".*[^a-zA-Z0-9].*",
            "Add special symbols like !, @, #, $, %, ^, &, * to increase complexity.",
            20
        );
    }
}
