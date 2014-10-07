package org.jboss.windup.config;

/**
 * Each addon that contains rules will implement this interface, and by doing so provide some basic metadata about their
 * contents.
 * 
 * @author Jess Sightler <jesse.sightler@gmail.com>
 */
public interface WindupRulesetMetadata
{
    /**
     * Returns a short identifier, that should uniquely identify a ruleset.
     * 
     * This should be shorter and more concise than the addon id itself.
     */
    public String getRuleSetID();
}
