package org.apache.wicket.validation;

import java.util.*;
import org.apache.wicket.util.lang.*;

public class ValidationError implements IValidationError{
    private static final long serialVersionUID=1L;
    private static final Map<String,Object> EMPTY_VARS;
    private List<String> keys;
    private Map<String,Object> vars;
    private String message;
    public ValidationError addMessageKey(final String key){
        Args.notEmpty((CharSequence)key,"key");
        if(this.keys==null){
            this.keys=(List<String>)new ArrayList(1);
        }
        this.keys.add(key);
        return this;
    }
    public ValidationError setVariable(final String name,final Object value){
        Args.notEmpty((CharSequence)name,"name");
        Args.notNull(value,"value");
        this.getVariables().put(name,value);
        return this;
    }
    public final Map<String,Object> getVariables(){
        if(this.vars==null){
            this.vars=(Map<String,Object>)new HashMap(2);
        }
        return this.vars;
    }
    public final ValidationError setVariables(final Map<String,Object> vars){
        Args.notNull((Object)vars,"vars");
        this.vars=vars;
        return this;
    }
    public final String getErrorMessage(final IErrorMessageSource messageSource){
        String errorMessage=null;
        if(this.keys!=null){
            for(final String key : this.keys){
                errorMessage=messageSource.getMessage(key);
                if(errorMessage!=null){
                    break;
                }
            }
        }
        if(errorMessage==null&&this.message!=null){
            errorMessage=this.message;
        }
        if(errorMessage!=null){
            final Map<String,Object> p=(this.vars!=null)?this.vars:ValidationError.EMPTY_VARS;
            errorMessage=messageSource.substitute(errorMessage,p);
        }
        return errorMessage;
    }
    public final String getMessage(){
        return this.message;
    }
    public final ValidationError setMessage(final String message){
        Args.notNull((Object)message,"message");
        this.message=message;
        return this;
    }
    public List<String> getKeys(){
        if(this.keys==null){
            return (List<String>)Collections.emptyList();
        }
        return (List<String>)Collections.unmodifiableList(this.keys);
    }
    public String toString(){
        final StringBuilder tostring=new StringBuilder();
        tostring.append("[").append(Classes.simpleName(this.getClass()));
        tostring.append(" message=[").append(this.message);
        tostring.append("], keys=[");
        if(this.keys!=null){
            final Iterator<String> i=(Iterator<String>)this.keys.iterator();
            while(i.hasNext()){
                tostring.append((String)i.next());
                if(i.hasNext()){
                    tostring.append(", ");
                }
            }
        }
        else{
            tostring.append("null");
        }
        tostring.append("], variables=[");
        if(this.vars!=null){
            final Iterator<Map.Entry<String,Object>> j=(Iterator<Map.Entry<String,Object>>)this.vars.entrySet().iterator();
            while(j.hasNext()){
                final Map.Entry<String,Object> e=(Map.Entry<String,Object>)j.next();
                tostring.append("[").append((String)e.getKey()).append("=").append(e.getValue()).append("]");
                if(j.hasNext()){
                    tostring.append(",");
                }
            }
        }
        else{
            tostring.append("null");
        }
        tostring.append("]");
        tostring.append("]");
        return tostring.toString();
    }
    static{
        EMPTY_VARS=Collections.emptyMap();
    }
}
