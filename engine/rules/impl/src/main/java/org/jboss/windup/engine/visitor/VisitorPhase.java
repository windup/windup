package org.jboss.windup.engine.visitor;

public enum VisitorPhase
{
    /**
     * Called during resource discovery (finding all files in archives, etc)
     */
    Discovery(100),
    /**
     * Called to perform basic analysis (extract all method names from class files, extract metadata from xml files, etc)
     */
    InitialAnalysis(200),
    /**
     * Perform high-level composition operations on the graph.
     * 
     * Eg, these may attach metadata from XML files to related Java classes, or perform other high-level graph operations, now that
     * all metadata has been extracted
     */
    Composition(300),
    /**
     * Migration rules will attach data to the graph associated with migration. This could include:
     * 
     *  - Hints to migrators for manual migration
     *  - Automated migration of schemas or source segments
     *  - Blacklists to indicate vendor specific APIs
     */
    MigrationRules(400),
    /**
     * Reporting visitors produce reports based upon the information contained within the graph
     */
    Reporting(500);
    
    
    private int priority;
    VisitorPhase(int priority) {
        this.priority = priority;
    }
    public int getPriority()
    {
        return priority;
    }
}
