package org.jboss.windup.rules.apps.summit.demo.rules;

import org.jboss.windup.reporting.model.TransformationQuickfixChangeModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents the change to the weblogic-application.xml file necessary for 
 * quickfixing application lifecycle listeners.
 */
@TypeValue(WeblogicApplicationLifecycleXMLChangeModel.TYPE)
public interface WeblogicApplicationLifecycleXMLChangeModel extends TransformationQuickfixChangeModel 
{
	String TYPE = "WeblogicApplicationLifecycleXMLChangeModel";
    String TYPE_PREFIX = TYPE + ":";
    
    /**
     * Returns a preview of what this change would look like if it were to be applied.
     */
    @JavaHandler
    String getPreview();
    /**
     * Applies this change to the underlying source file.
     */
    @JavaHandler
    void applyChange();
    
    abstract class Impl implements WeblogicApplicationLifecycleXMLChangeModel, JavaHandlerContext<Vertex>
    {
        @Override
        public String getPreview()
        {
        	/*SerializableFunction<WeblogicApplicationLifecycleXMLChangeModel> supplier = getPreviewer();
        	String preview = supplier.apply(this);
        	//System.out.println(supplier.get());
        	//FileModel xmlFileModel = getFile();
            return "Previewer change: " + preview;*/
        	return "";
        }
        
        @Override
        public void applyChange() {
        	System.out.println("Applying: " + getPreview());
        }
    }
}