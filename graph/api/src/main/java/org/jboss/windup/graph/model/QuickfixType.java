package org.jboss.windup.graph.model;

/**
 * QuickfixType specifies what type of Quickfix it is.
 * 
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 *
 */
public enum QuickfixType
{

    /**
     * It searches for token and replaces it by another token 
     */
    REPLACE("Quickfix REPLACE token"),
    /**
     * It searches for a token and deletes whole found line
     */
    DELETE_LINE("Quickfix DELETE_LINE"),
    /**
     * It searches for a token and inserts a new line after found line
     */
    INSERT_LINE("Quickfix INSERT_LINE");
    
    private String description;
    
    private QuickfixType(String description)
    {
        this.description = description;
    }
    
    @Override
    public String toString()
    {
        return description;
    }
}
