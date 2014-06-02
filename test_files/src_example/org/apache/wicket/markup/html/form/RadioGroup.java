package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.*;
import org.apache.wicket.util.convert.*;
import org.apache.wicket.markup.*;

public class RadioGroup<T> extends FormComponent<T> implements IOnChangeListener{
    private static final long serialVersionUID=1L;
    public RadioGroup(final String id){
        super(id);
        this.setRenderBodyOnly(true);
    }
    public RadioGroup(final String id,final IModel<T> model){
        super(id,model);
        this.setRenderBodyOnly(true);
    }
    protected boolean wantOnSelectionChangedNotifications(){
        return false;
    }
    protected boolean getStatelessHint(){
        return !this.wantOnSelectionChangedNotifications()&&super.getStatelessHint();
    }
    protected T convertValue(final String[] input) throws ConversionException{
        if(input==null||input.length<=0){
            return null;
        }
        final String value=input[0];
        final Radio<T> choice=this.visitChildren((Class<?>)Radio.class,(org.apache.wicket.util.visit.IVisitor<Component,Radio<T>>)new IVisitor<Radio<T>,Radio<T>>(){
            public void component(final Radio<T> radio,final IVisit<Radio<T>> visit){
                if(radio.getValue().equals(value)){
                    visit.stop((Object)radio);
                }
            }
        });
        if(choice==null){
            throw new WicketRuntimeException("submitted http post value ["+value+"] for RadioGroup component ["+this.getPath()+"] is illegal because it does not point to a Radio component. "+"Due to this the RadioGroup component cannot resolve the selected Radio component pointed to by the illegal value. A possible reason is that component hierarchy changed between rendering and form submission.");
        }
        return choice.getModelObject();
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.remove("disabled");
        tag.remove("name");
    }
    public final void onSelectionChanged(){
        this.convertInput();
        this.updateModel();
        this.onSelectionChanged(this.getDefaultModelObject());
    }
    protected void onSelectionChanged(final Object newSelection){
    }
}
