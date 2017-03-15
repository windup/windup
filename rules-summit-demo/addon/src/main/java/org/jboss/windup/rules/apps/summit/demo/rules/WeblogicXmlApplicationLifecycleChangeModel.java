package org.jboss.windup.rules.apps.summit.demo.rules;


import static org.jboss.windup.rules.apps.summit.demo.rules.WeblogicXmlApplicationLifecycleChangeModel.TYPE_VALUE;

import org.jboss.windup.reporting.model.TransformationQuickfixChangeModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(TYPE_VALUE)
public interface WeblogicXmlApplicationLifecycleChangeModel extends TransformationQuickfixChangeModel
{
	String TYPE_VALUE = "WeblogicXmlApplicationLifecycleChange";

    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    XmlFileModel getFile();

    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    void setFile(XmlFileModel file);
	
	@JavaHandler
	String preview();
	
	@JavaHandler
	void apply();
	
	abstract class Impl implements WeblogicXmlApplicationLifecycleChangeModel, JavaHandlerContext<Vertex>
    {
       @Override
       public String preview() 
       {
    	   return "Preview...";
       }

       @Override
    	public void apply() {
    	}
    }
}
