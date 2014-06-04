package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.convert.*;
import java.util.*;

public class CheckBox extends FormComponent<Boolean> implements IOnChangeListener{
    private static final long serialVersionUID=1L;
    public CheckBox(final String id){
        this(id,null);
    }
    public CheckBox(final String id,final IModel<Boolean> model){
        super(id,model);
        this.setType((Class<?>)Boolean.class);
    }
    public void onSelectionChanged(){
        this.convertInput();
        this.updateModel();
        this.onSelectionChanged(this.getModelObject());
    }
    protected void onSelectionChanged(final Boolean newSelection){
    }
    protected boolean wantOnSelectionChangedNotifications(){
        return false;
    }
    protected boolean getStatelessHint(){
        return !this.wantOnSelectionChangedNotifications()&&super.getStatelessHint();
    }
    protected void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"input");
        this.checkComponentTagAttribute(tag,"type","checkbox");
        final String value=this.getValue();
        final IConverter<Boolean> converter=this.getConverter((Class<Boolean>)Boolean.class);
        final Boolean checked=(Boolean)converter.convertToObject(value,this.getLocale());
        if(Boolean.TRUE.equals(checked)){
            tag.put("checked",(CharSequence)"checked");
        }
        else{
            tag.remove("checked");
        }
        tag.remove("value");
        if(this.wantOnSelectionChangedNotifications()){
            final CharSequence url=this.urlFor(IOnChangeListener.INTERFACE,new PageParameters());
            final Form<?> form=this.findParent((Class<Form<?>>)Form.class);
            if(form!=null){
                tag.put("onclick",form.getJsForInterfaceUrl(url));
            }
            else{
                tag.put("onclick",(CharSequence)("window.location.href='"+(Object)url+((url.toString().indexOf(63)>-1)?"&":"?")+this.getInputName()+"=' + this.checked;"));
            }
        }
        super.onComponentTag(tag);
    }
    public final <C> IConverter<C> getConverter(final Class<C> type){
        if(Boolean.class.equals(type)){
            final IConverter<C> converter=(IConverter<C>)CheckBoxConverter.INSTANCE;
            return converter;
        }
        return super.getConverter(type);
    }
    private static class CheckBoxConverter implements IConverter<Boolean>{
        private static final long serialVersionUID=1L;
        private static final IConverter<Boolean> INSTANCE;
        public Boolean convertToObject(final String value,final Locale locale){
            if("on".equals(value)||"true".equals(value)){
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        public String convertToString(final Boolean value,final Locale locale){
            return value.toString();
        }
        static{
            INSTANCE=(IConverter)new CheckBoxConverter();
        }
    }
}
