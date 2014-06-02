package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;
import java.util.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.mapper.parameter.*;

public class Check<T> extends LabeledWebMarkupContainer{
    private static final long serialVersionUID=1L;
    private static final String ATTR_DISABLED="disabled";
    private int uuid;
    private final CheckGroup<T> group;
    public Check(final String id){
        this(id,null,null);
    }
    public Check(final String id,final IModel<T> model){
        this(id,model,null);
    }
    public Check(final String id,final CheckGroup<T> group){
        this(id,null,group);
    }
    public Check(final String id,final IModel<T> model,final CheckGroup<T> group){
        super(id,model);
        this.uuid=-1;
        this.group=group;
        this.setOutputMarkupId(true);
    }
    public String getValue(){
        if(this.uuid<0){
            this.uuid=this.getPage().getAutoIndex();
        }
        return "check"+this.uuid;
    }
    protected CheckGroup<T> getGroup(){
        CheckGroup<T> group=this.group;
        if(group==null){
            group=this.findParent((Class<CheckGroup<T>>)CheckGroup.class);
            if(group==null){
                throw new WicketRuntimeException("Check component ["+this.getPath()+"] cannot find its parent CheckGroup");
            }
        }
        return group;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        this.checkComponentTag(tag,"input");
        this.checkComponentTagAttribute(tag,"type","checkbox");
        final CheckGroup<?> group=this.getGroup();
        final String uuid=this.getValue();
        tag.put("name",(CharSequence)group.getInputName());
        tag.put("value",(CharSequence)uuid);
        final Collection<?> collection=(Collection<?>)group.getDefaultModelObject();
        if(collection==null){
            throw new WicketRuntimeException("CheckGroup ["+group.getPath()+"] contains a null model object, must be an object of type java.util.Collection");
        }
        if(group.hasRawInput()){
            final String raw=group.getRawInput();
            if(!Strings.isEmpty((CharSequence)raw)){
                final String[] arr$;
                final String[] values=arr$=raw.split(";");
                for(final String value : arr$){
                    if(uuid.equals(value)){
                        tag.put("checked",(CharSequence)"checked");
                    }
                }
            }
        }
        else if(collection.contains(this.getDefaultModelObject())){
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
        if(!this.isActionAuthorized(Check.ENABLE)||!this.isEnabledInHierarchy()||!group.isEnabledInHierarchy()){
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
    public Check<T> setLabel(final IModel<String> labelModel){
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
