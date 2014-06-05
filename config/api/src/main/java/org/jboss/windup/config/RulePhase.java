package org.jboss.windup.config;

public enum RulePhase
{
    /**
     * Called during resource discovery (finding all files in archives, etc)
     */
    DISCOVERY(100),
    /**
     * Called to perform basic analysis (extract all method names from class files, extract metadata from xml files,
     * etc)
     */
    INITIAL_ANALYSIS(200),
    /**
     * Perform high-level composition operations on the graph.
     * 
     * Eg, these may attach metadata from XML files to related Java classes, or perform other high-level graph
     * operations, now that all metadata has been extracted
     */
    COMPOSITION(300),
    /**
     * Migration rules will attach data to the graph associated with migration. This could include:
     * 
     * - Hints to migrators for manual migration - Automated migration of schemas or source segments - Blacklists to
     * indicate vendor specific APIs
     */
    MIGRATION_RULES(400),
    /**
     * Reporting visitors produce reports based upon the information contained within the graph
     */
    REPORTING(500),

    /**
     * Clean up resources and close streams.
     */
    FINALIZE(600);

    private int priority;

    RulePhase(int priority)
    {
        this.priority = priority;
    }

    public int getPriority()
    {
        return priority;
    }
}
