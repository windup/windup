package org.jboss.windup.rules.apps.xml.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.FileReferenceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("XsltTransformation")
public interface XsltTransformationModel extends WindupVertexFrame
{
        public static final String PROPERTY_LOCATION = "location";
        public static final String PROPERTY_EXTENSION = "extension";
        public static final String PROPERTY_DESCRIPTION = "description";
        public static final String PROPERTY_FILE_SOURCE = "file_source";
        public static final String PROPERTY_FILE_RESULT = "file_result";

        @Property(PROPERTY_LOCATION)
        public String getSourceLocation();

        @Property(PROPERTY_LOCATION)
        public void setSourceLocation(String location);
        
        @Property(PROPERTY_EXTENSION)
        public String getExtension();

        @Property(PROPERTY_EXTENSION)
        public void setExtension(String extension);
        
        @Property(PROPERTY_DESCRIPTION)
        public String getDescription();

        @Property(PROPERTY_DESCRIPTION)
        public void setDescription(String description);
        
        
        
        @Adjacency(label = PROPERTY_FILE_SOURCE, direction = Direction.OUT)
        FileModel getSourceFile();

        @Adjacency(label = PROPERTY_FILE_SOURCE, direction = Direction.OUT)
        void setSourceFile(FileModel file);
        
        @Property(PROPERTY_FILE_RESULT)
        String getResult();

        @Property(PROPERTY_FILE_RESULT)
        void setResult(String path);
}