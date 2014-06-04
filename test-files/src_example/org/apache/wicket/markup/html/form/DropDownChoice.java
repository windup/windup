package org.apache.wicket.markup.html.form;

import java.util.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.handler.*;
import org.apache.wicket.request.*;

public class DropDownChoice<T> extends AbstractSingleSelectChoice<T> implements IOnChangeListener{
    private static final long serialVersionUID=1L;
    public DropDownChoice(final String id){
        super(id);
    }
    public DropDownChoice(final String id,final List<? extends T> choices){
        super(id,choices);
    }
    public DropDownChoice(final String id,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
    }
    public DropDownChoice(final String id,final IModel<T> model,final List<? extends T> choices){
        super(id,model,choices);
    }
    public DropDownChoice(final String id,final IModel<T> model,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,renderer);
    }
    public DropDownChoice(final String id,final IModel<? extends List<? extends T>> choices){
        super(id,choices);
    }
    public DropDownChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices){
        super(id,model,choices);
    }
    public DropDownChoice(final String id,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
    }
    public DropDownChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,renderer);
    }
    public final void onSelectionChanged(){
        this.convertInput();
        this.updateModel();
        this.onSelectionChanged(this.getModelObject());
    }
    protected void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"select");
        if(this.wantOnSelectionChangedNotifications()){
            final CharSequence url=this.urlFor((IRequestHandler)new ListenerInterfaceRequestHandler(new PageAndComponentProvider(this.getPage(),this,new PageParameters()),IOnChangeListener.INTERFACE));
            final Form<?> form=this.findParent((Class<Form<?>>)Form.class);
            if(form!=null){
                tag.put("onchange",form.getJsForInterfaceUrl((CharSequence)url.toString()));
            }
            else{
                tag.put("onchange",(CharSequence)("window.location.href='"+(Object)url+((url.toString().indexOf(63)>-1)?"&":"?")+this.getInputName()+"=' + this.options[this.selectedIndex].value;"));
            }
        }
        super.onComponentTag(tag);
    }
    protected void onSelectionChanged(final T newSelection){
    }
    protected boolean wantOnSelectionChangedNotifications(){
        return false;
    }
    protected boolean getStatelessHint(){
        return !this.wantOnSelectionChangedNotifications()&&super.getStatelessHint();
    }
}
