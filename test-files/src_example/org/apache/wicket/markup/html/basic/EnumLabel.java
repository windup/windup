package org.apache.wicket.markup.html.basic;

import org.apache.wicket.markup.html.*;
import java.io.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.*;

public class EnumLabel<T extends Enum<T>> extends WebComponent{
    private static final long serialVersionUID=1L;
    public EnumLabel(final String id){
        super(id);
    }
    public EnumLabel(final String id,final T value){
        this(id,(IModel)new Model(value));
    }
    public EnumLabel(final String id,final IModel<T> model){
        super(id,model);
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)this.getStringValue());
    }
    private String getStringValue(){
        final T value=this.getModelObject();
        final String converted=(value!=null)?this.getString(this.resourceKey(value)):this.nullValue();
        return this.getDefaultModelObjectAsString(converted);
    }
    protected String resourceKey(final T value){
        return value.getDeclaringClass().getSimpleName()+"."+value.name();
    }
    protected String nullValue(){
        return "";
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.setType(XmlTag.TagType.OPEN);
    }
    public final IModel<T> getModel(){
        return (IModel<T>)this.getDefaultModel();
    }
    public final void setModel(final IModel<T> model){
        this.setDefaultModel(model);
    }
    public final T getModelObject(){
        return (T)this.getDefaultModelObject();
    }
    public final void setModelObject(final T object){
        this.setDefaultModelObject(object);
    }
}
