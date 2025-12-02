import java.util.*;

interface Condition {
    boolean evaluate(Map<String, Object> facts);
}

class TemperatureCondition implements Condition {
    @Override
    public boolean evaluate(Map<String, Object> facts) {
        if (facts.containsKey("temperature") && facts.get("temperature") instanceof Integer) {
            int temp = (int) facts.get("temperature");
            return temp > 30; // Check if the temperature is above 30
        }
        return false;
    }
}

interface Action {
    void execute(Map<String, Object> facts);
}

class SendAlertAction implements Action {
    @Override
    public void execute(Map<String, Object> facts) {
        String location = (String) facts.getOrDefault("location", "Unknown Location");
        System.out.println("[ACTION] ALERT ISSUED: High temperature detected in " + location);
    }
}

class Rule {
    String id;
    String name;
    Condition condition;
    Action action;

    public Rule(String name, Condition condition, Action action) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.condition = condition;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public boolean apply(Map<String, Object> facts) {
        if (condition.evaluate(facts)) {
            System.out.println("Rule Matched: " + name);
            action.execute(facts);
            return true;
        }
        return false;
    }
}

class RuleEngine {
    private final List<Rule> rules;

    public RuleEngine() {
        this.rules = new ArrayList<>();
    }

    public void addRule(Rule rule) {
        if (rule != null) {
            this.rules.add(rule);
        }
    }

    /**
     * Processes a set of facts against all registered rules.
     * Time Complexity: O(N * C), where N is number of rules and C is the complexity of evaluating a single condition.
     */
    public void process(Map<String, Object> facts) {
        System.out.println("--- Starting Rule Engine Process ---");
        for (Rule rule : rules) {
            // apply() handles the internal logic of condition check and action execution.
            rule.apply(facts);
        }
        System.out.println("--- Finished Rule Engine Process ---");
    }
}

public class RuleEngineSystem {
    public static void main(String[] args) {
        // 1. Set up the Engine and Rules
        RuleEngine engine = new RuleEngine();

        Condition highTempCondition = new TemperatureCondition();
        Action sendAlertAction = new SendAlertAction();

        // Create the rule
        Rule highTempAlertRule = new Rule("High Temp Alert Rule", highTempCondition, sendAlertAction);

        engine.addRule(highTempAlertRule);

        // 2. Prepare Facts
        Map<String, Object> sensorFacts = new HashMap<>();
        sensorFacts.put("temperature", 35);
        sensorFacts.put("location", "Server Room A");

        // 3. Process Facts
        engine.process(sensorFacts);

        // Example with facts that don't match the condition:
        Map<String, Object> normalFacts = new HashMap<>();
        normalFacts.put("temperature", 20);
        normalFacts.put("location", "Office B");

        engine.process(normalFacts);
    }
}
