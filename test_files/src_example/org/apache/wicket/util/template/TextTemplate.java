package org.apache.wicket.util.template;

import org.apache.wicket.util.resource.*;
import java.util.*;
import org.apache.wicket.util.string.interpolator.*;

public abstract class TextTemplate extends AbstractStringResourceStream{
    private static final long serialVersionUID=1L;
    public TextTemplate(){
        super();
    }
    public TextTemplate(final String contentType){
        super(contentType);
    }
    public String asString(final Map<String,?> variables){
        if(variables!=null){
            return new MapVariableInterpolator(this.getString(),(Map)variables).toString();
        }
        return this.getString();
    }
    public String asString(){
        return this.getString();
    }
    public abstract String getString();
    public abstract TextTemplate interpolate(final Map<String,?> p0);
}
