package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;
import org.apache.wicket.util.string.*;

public class EnumChoiceRenderer<T extends Enum<T>> implements IChoiceRenderer<T>{
    private static final long serialVersionUID=1L;
    private final Component resourceSource;
    public EnumChoiceRenderer(){
        super();
        this.resourceSource=null;
    }
    public EnumChoiceRenderer(final Component resourceSource){
        super();
        this.resourceSource=resourceSource;
    }
    public final Object getDisplayValue(final T object){
        final String key=this.resourceKey(object);
        String value;
        if(this.resourceSource!=null){
            value=this.resourceSource.getString(key);
        }
        else{
            value=Application.get().getResourceSettings().getLocalizer().getString(key,null);
        }
        return this.postprocess(value);
    }
    protected String resourceKey(final T object){
        return object.getDeclaringClass().getSimpleName()+"."+object.name();
    }
    protected CharSequence postprocess(final String value){
        return Strings.escapeMarkup((CharSequence)value);
    }
    public String getIdValue(final T object,final int index){
        return object.name();
    }
}
