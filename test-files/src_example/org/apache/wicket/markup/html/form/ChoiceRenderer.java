package org.apache.wicket.markup.html.form;

import org.apache.wicket.util.lang.*;

public class ChoiceRenderer<T> implements IChoiceRenderer<T>{
    private static final long serialVersionUID=1L;
    private final String displayExpression;
    private final String idExpression;
    public ChoiceRenderer(){
        super();
        this.displayExpression=null;
        this.idExpression=null;
    }
    public ChoiceRenderer(final String displayExpression){
        super();
        this.displayExpression=displayExpression;
        this.idExpression=null;
    }
    public ChoiceRenderer(final String displayExpression,final String idExpression){
        super();
        this.displayExpression=displayExpression;
        this.idExpression=idExpression;
    }
    public Object getDisplayValue(final T object){
        Object returnValue=object;
        if(this.displayExpression!=null&&object!=null){
            returnValue=PropertyResolver.getValue(this.displayExpression,object);
        }
        if(returnValue==null){
            return "";
        }
        return returnValue;
    }
    public String getIdValue(final T object,final int index){
        if(this.idExpression==null){
            return Integer.toString(index);
        }
        if(object==null){
            return "";
        }
        final Object returnValue=PropertyResolver.getValue(this.idExpression,object);
        if(returnValue==null){
            return "";
        }
        return returnValue.toString();
    }
}
