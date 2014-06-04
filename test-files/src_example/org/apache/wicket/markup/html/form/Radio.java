package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.util.string.*;

public class Radio<T> extends LabeledWebMarkupContainer{
    private static final long serialVersionUID=1L;
    private static final String ATTR_DISABLED="disabled";
    private int uuid;
    private final RadioGroup<T> group;
    public Radio(final String id){
        this(id,null,null);
    }
    public Radio(final String id,final IModel<T> model){
        this(id,model,null);
    }
    public Radio(final String id,final RadioGroup<T> group){
        this(id,null,group);
    }
    public Radio(final String id,final IModel<T> model,final RadioGroup<T> group){
        super(id,model);
        this.uuid=-1;
        this.group=group;
        this.setOutputMarkupId(true);
    }
    public String getValue(){
        if(this.uuid<0){
            this.uuid=this.getPage().getAutoIndex();
        }
        return "radio"+this.uuid;
    }
    protected RadioGroup<T> getGroup(){
        RadioGroup<T> group=this.group;
        if(group==null){
            group=this.findParent((Class<RadioGroup<T>>)RadioGroup.class);
            if(group==null){
                throw new WicketRuntimeException("Radio component ["+this.getPath()+"] cannot find its parent RadioGroup. All Radio components must be a child of or below in the hierarchy of a RadioGroup component.");
            }
        }
        return group;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        this.checkComponentTag(tag,"input");
        this.checkComponentTagAttribute(tag,"type","radio");
        final String value=this.getValue();
        final RadioGroup<?> group=this.getGroup();
        tag.put("name",(CharSequence)group.getInputName());
        tag.put("value",(CharSequence)value);
        if(group.hasRawInput()){
            final String rawInput=group.getRawInput();
            if(rawInput!=null&&rawInput.equals(value)){
                tag.put("checked",(CharSequence)"checked");
            }
        }
        else if(group.getModelComparator().compare(group,this.getDefaultModelObject())){
            tag.put("checked",(CharSequence)"checked");
        }
        if(group.wantOnSelectionChangedNotifications()){
            final CharSequence url=group.urlFor(IOnChangeListener.INTERFACE,new PageParameters());
            final Form<?> form=group.findParent((Class<Form<?>>)Form.class);
            if(form!=null){
                tag.put("onclick",form.getJsForInterfaceUrl(url));
            }
            else{
                tag.put("onclick",(CharSequence)("window.location.href='"+(Object)url+((url.toString().indexOf(63)>-1)?"&":"?")+group.getInputName()+"=' + this.value;"));
            }
        }
        if(!this.isEnabledInHierarchy()){
            tag.put("disabled",(CharSequence)"disabled");
        }
        final String marker="wicket-"+this.getGroup().getMarkupId();
        String clazz=tag.getAttribute("class");
        if(Strings.isEmpty((CharSequence)clazz)){
            clazz=marker;
        }
        else{
            clazz=clazz+" "+marker;
        }
        tag.put("class",(CharSequence)clazz);
    }
    public Radio<T> setLabel(final IModel<String> labelModel){
        this.setLabelInternal(labelModel);
        return this;
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
    protected boolean getStatelessHint(){
        return false;
    }
}
