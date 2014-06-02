package org.apache.wicket;

import org.apache.wicket.util.lang.*;
import java.io.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;

public class AttributeModifier extends Behavior implements IClusterable{
    public static final String VALUELESS_ATTRIBUTE_ADD;
    public static final String VALUELESS_ATTRIBUTE_REMOVE;
    private static final long serialVersionUID=1L;
    private final String attribute;
    private final IModel<?> replaceModel;
    public AttributeModifier(final String attribute,final boolean addAttributeIfNotPresent,final IModel<?> replaceModel){
        this(attribute,replaceModel);
    }
    public AttributeModifier(final String attribute,final IModel<?> replaceModel){
        super();
        Args.notNull((Object)attribute,"attribute");
        this.attribute=attribute;
        this.replaceModel=replaceModel;
    }
    public AttributeModifier(final String attribute,final Serializable value){
        this(attribute,Model.of(value));
    }
    public final void detach(final Component component){
        if(this.replaceModel!=null){
            this.replaceModel.detach();
        }
    }
    public final String getAttribute(){
        return this.attribute;
    }
    public final void onComponentTag(final Component component,final ComponentTag tag){
        if(tag.getType()!=XmlTag.TagType.CLOSE){
            this.replaceAttributeValue(component,tag);
        }
    }
    public final void replaceAttributeValue(final Component component,final ComponentTag tag){
        if(this.isEnabled(component)){
            final IValueMap attributes=tag.getAttributes();
            final Object replacementValue=this.getReplacementOrNull(component);
            if(AttributeModifier.VALUELESS_ATTRIBUTE_ADD==replacementValue){
                attributes.put((Object)this.attribute,(Object)null);
            }
            else if(AttributeModifier.VALUELESS_ATTRIBUTE_REMOVE==replacementValue){
                attributes.remove((Object)this.attribute);
            }
            else{
                final String value=this.toStringOrNull(attributes.get((Object)this.attribute));
                final String newValue=this.newValue(value,this.toStringOrNull(replacementValue));
                if(newValue!=null){
                    attributes.put((Object)this.attribute,(Object)newValue);
                }
            }
        }
    }
    public String toString(){
        return "[AttributeModifier attribute="+this.attribute+", replaceModel="+this.replaceModel+"]";
    }
    private Object getReplacementOrNull(final Component component){
        IModel<?> model=this.replaceModel;
        if(model instanceof IComponentAssignedModel){
            model=(IModel<?>)((IComponentAssignedModel)model).wrapOnAssignment(component);
        }
        return (model!=null)?model.getObject():null;
    }
    private String toStringOrNull(final Object replacementValue){
        return (replacementValue!=null)?replacementValue.toString():null;
    }
    protected final IModel<?> getReplaceModel(){
        return this.replaceModel;
    }
    protected String newValue(final String currentValue,final String replacementValue){
        return replacementValue;
    }
    public static AttributeModifier replace(final String attributeName,final IModel<?> value){
        Args.notEmpty((CharSequence)attributeName,"attributeName");
        return new AttributeModifier(attributeName,value);
    }
    public static AttributeModifier replace(final String attributeName,final Serializable value){
        Args.notEmpty((CharSequence)attributeName,"attributeName");
        return new AttributeModifier(attributeName,value);
    }
    public static AttributeAppender append(final String attributeName,final IModel<?> value){
        Args.notEmpty((CharSequence)attributeName,"attributeName");
        return new AttributeAppender(attributeName,value).setSeparator(" ");
    }
    public static AttributeAppender append(final String attributeName,final Serializable value){
        Args.notEmpty((CharSequence)attributeName,"attributeName");
        return append(attributeName,Model.of(value));
    }
    public static AttributeAppender prepend(final String attributeName,final IModel<?> value){
        Args.notEmpty((CharSequence)attributeName,"attributeName");
        return new AttributeAppender(attributeName,value){
            private static final long serialVersionUID=1L;
            protected String newValue(final String currentValue,final String replacementValue){
                return super.newValue(replacementValue,currentValue);
            }
        }.setSeparator(" ");
    }
    public static AttributeAppender prepend(final String attributeName,final Serializable value){
        Args.notEmpty((CharSequence)attributeName,"attributeName");
        return prepend(attributeName,Model.of(value));
    }
    public static AttributeModifier remove(final String attributeName){
        Args.notEmpty((CharSequence)attributeName,"attributeName");
        return replace(attributeName,Model.of(AttributeModifier.VALUELESS_ATTRIBUTE_REMOVE));
    }
    static{
        VALUELESS_ATTRIBUTE_ADD=new String("VA_ADD");
        VALUELESS_ATTRIBUTE_REMOVE=new String("VA_REMOVE");
    }
}
