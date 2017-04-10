package org.jboss.windup.reporting.model;

import static org.jboss.windup.reporting.model.LocationDataModel.TYPE_VALUE;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(TYPE_VALUE)
public interface LocationDataModel extends WindupVertexFrame 
{
	String TYPE_VALUE = "LocationData";
	String START_LINE = TYPE_VALUE + "-startLine";
	String START_COLUMN = TYPE_VALUE + "-startColumn";
	String END_LINE = TYPE_VALUE + "-endLine";
	String END_COLUMN = TYPE_VALUE + "-endColumn";
	
	@Property(START_LINE)
    void setStartLine(int startLine);
    
    @Property(START_LINE)
    int getStartLine();
    
    @Property(START_COLUMN)
    void setStartColumnn(int startColumn);
    
    @Property(START_COLUMN)
    int getStartColumn();
    
	@Property(END_LINE)
    void setEndLine(int endLine);
    
    @Property(END_LINE)
    int getEndLine();
    
    @Property(END_COLUMN)
    void setEndColumnn(int endColumn);
    
    @Property(END_COLUMN)
    int getEndColumn();
}
