package org.jboss.windup.rules.apps.java.reporting.freemarker.dto;

/**
 * A data transfer object carying information about the given hint and number of occurences
 */
public class HintWithOccurence {
    private final String hint;
    private final String ruleId;
    private int occurences;

    public HintWithOccurence(String hint, String ruleId, int occurences) {
        this.hint = hint;
        this.ruleId = ruleId;
        this.occurences = occurences;
    }

    public void addOccurence() {
        this.occurences += 1;
    }
}
