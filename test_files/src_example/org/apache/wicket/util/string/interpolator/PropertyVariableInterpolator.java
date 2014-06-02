package org.apache.wicket.util.string.interpolator;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;

public class PropertyVariableInterpolator extends VariableInterpolator{
    private static final long serialVersionUID=1L;
    private final Object model;
    public PropertyVariableInterpolator(final String string,final Object model){
        super(string);
        this.model=model;
    }
    @Deprecated
    public static String interpolate(final String string,final Object object){
        return new PropertyVariableInterpolator(string,object).toString();
    }
    protected String getValue(final String variableName){
        final Object value=PropertyResolver.getValue(variableName,this.model);
        if(value!=null){
            return this.toString(value);
        }
        return null;
    }
    protected String toString(final Object value){
        return Strings.toString(value);
    }
}
