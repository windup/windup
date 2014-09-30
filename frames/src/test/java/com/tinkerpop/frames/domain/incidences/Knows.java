package com.tinkerpop.frames.domain.incidences;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.frames.Domain;
import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.Range;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Knows {

    @Property("weight")
    public Float getWeight();

    @Property("weight")
    public Float setWeight(float weight);

    @OutVertex
    public Person getOut();


    @InVertex
    public Person getIn();

    @Domain
    public Person getDomain();
    
    @Range
    public Person getRange();
    
    @JavaHandler
    public String getNames();

    abstract class Impl implements Knows, JavaHandlerContext<Edge> {

    	@Override
    	@JavaHandler
    	public String getNames() {
    		return getDomain().getName() + "<->" + getRange().getName();
    	}
    }


}
