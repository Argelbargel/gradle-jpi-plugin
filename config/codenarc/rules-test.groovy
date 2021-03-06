ruleset {
    ruleset('rulesets/junit.xml') {
        // Spock ...
        exclude 'JUnitPublicNonTestMethod'
    }

    ruleset('file:config/codenarc/rules.groovy') {
        // Spock encourages to violate this rule
        exclude 'MethodName'
        // OK for tests
        exclude 'Instanceof'
        // Spock ...
        exclude 'UnnecessaryBooleanExpression'
    }
}
