package org.apache.wicket.behavior;

import org.apache.wicket.*;
import org.apache.wicket.model.*;
import java.io.*;
import org.apache.wicket.util.string.*;

public class AttributeAppender extends AttributeModifier{
    private static final long serialVersionUID=1L;
    private String separator;
    public AttributeAppender(final String attribute,final boolean addAttributeIfNotPresent,final IModel<?> appendModel,final String separator){
        this(attribute,appendModel,separator);
    }
    public AttributeAppender(final String attribute,final IModel<?> replaceModel){
        super(attribute,replaceModel);
    }
    public AttributeAppender(final String attribute,final Serializable value){
        super(attribute,value);
    }
    public AttributeAppender(final String attribute,final IModel<?> appendModel,final String separator){
        super(attribute,appendModel);
        this.setSeparator(separator);
    }
    public String getSeparator(){
        return this.separator;
    }
    public AttributeAppender setSeparator(final String separator){
        this.separator=separator;
        return this;
    }
    protected String newValue(final String currentValue,final String appendValue){
        if(Strings.isEmpty((CharSequence)currentValue)){
            return (appendValue!=null)?appendValue:null;
        }
        if(Strings.isEmpty((CharSequence)appendValue)){
            return (currentValue!=null)?currentValue:null;
        }
        final StringBuilder sb=new StringBuilder(currentValue);
        sb.append((this.getSeparator()==null)?"":this.getSeparator());
        sb.append(appendValue);
        return sb.toString();
    }
    public String toString(){
        String attributeModifier=super.toString();
        attributeModifier=attributeModifier.substring(0,attributeModifier.length()-2)+", separator="+this.separator+"]";
        return attributeModifier;
    }
}
