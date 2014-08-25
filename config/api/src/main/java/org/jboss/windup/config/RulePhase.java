package org.jboss.windup.config;

/**
 * Lists the various phases of execution of Windup. The integer controls the order of phase execution.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public enum RulePhase
{
    /**
     * Called before resource discovery
     */
    PRE_DISCOVERY(90),

    /**
     * Called during resource discovery (finding all files in archives, etc)
     */
    DISCOVERY(100),

    /**
     * Called after resource discovery
     */
    POST_DISCOVERY(110),

    /**
     * Before initial analysis tasks
     */
    PRE_INITIAL_ANALYSIS(190),

    /**
     * Called to perform basic analysis (extract all method names from class files, extract metadata from xml files,
     * etc)
     */
    INITIAL_ANALYSIS(200),

    /**
     * After initial analysis tasks
     */
    POST_INITIAL_ANALYSIS(210),

    /**
     * Before the composition step
     */
    PRE_COMPOSITION(290),

    /**
     * Perform high-level composition operations on the graph.
     * 
     * Eg, these may attach metadata from XML files to related Java classes, or perform other high-level graph
     * operations, now that all metadata has been extracted
     */
    COMPOSITION(300),

    /**
     * After the composition step
     */
    POST_COMPOSITION(310),

    /**
     * Before the migration rules step
     */
    PRE_MIGRATION_RULES(390),

    /**
     * Migration rules will attach data to the graph associated with migration. This could include:
     * 
     * - Hints to migrators for manual migration - Automated migration of schemas or source segments - Blacklists to
     * indicate vendor specific APIs
     */
    MIGRATION_RULES(400),

    /**
     * After the migration rules
     */
    POST_MIGRATION_RULES(410),

    /**
     * Before report generation
     */
    PRE_REPORT_GENERATION(490),

    /**
     * Reporting visitors produce report data in the graph that will later be used by report rendering
     */
    REPORT_GENERATION(500),

    /**
     * After report generation
     */
    POST_REPORT_GENERATION(510),

    /**
     * Before report rendering
     */
    PRE_REPORT_RENDERING(590),

    /**
     * Actually renders the report into the expected
     */
    REPORT_RENDERING(600),

    /**
     * After report rendering
     */
    POST_REPORT_RENDERING(610),

    /**
     * Immediately before finalize
     */
    PRE_FINALIZE(690),

    /**
     * Clean up resources and close streams.
     */
    FINALIZE(700),

    /**
     * Immediately after finalize
     */
    POST_FINALIZE(710),

    /**
     * These rules will operate solely based the return the return value of {@link
     * org.jboss.windup.config.WindupRuleProvider.getExecuteAfter()} and {@link
     * org.jboss.windup.config.WindupRuleProvider.getExecuteBefore()}.
     * 
     * Depending upon the ordering specified by those methods, this could occur during any phase of execution.
     */
    IMPLICIT(Integer.MAX_VALUE);

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
