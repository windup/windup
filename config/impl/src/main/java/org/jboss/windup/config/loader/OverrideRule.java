package org.jboss.windup.config.loader;

import org.ocpsoft.rewrite.config.Rule;

public class OverrideRule {
    private final Rule rule;
    private boolean used = false;

    public OverrideRule(Rule rule) {
        this.rule = rule;
    }

    public Rule getRule() {
        return rule;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
