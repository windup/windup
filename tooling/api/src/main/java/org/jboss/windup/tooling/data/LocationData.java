package org.jboss.windup.tooling.data;

import java.io.Serializable;

public class LocationData implements Serializable {
	
    private static final long serialVersionUID = 1L;

    public static final String LOCATION_DATA_KEY = "locationDataKey";

    private final int startLine;
    private final int startColumn;
    private final int endLine;
    private final int endColumn;

    public LocationData(int startLine, int startColumn, int endLine, int endColumn) 
    {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public int getStartLine() 
    {
        return startLine;
    }

    public int getStartColumn() 
    {
        return startColumn;
    }

    public int getEndLine() 
    {
        return endLine;
    }

    public int getEndColumn() 
    {
        return endColumn;
    }

    @Override
    public String toString() 
    {
        return "[line " + startLine + ":"
                + startColumn + " to line " + endLine + ":"
                + endColumn + "]";
    }
}