package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.util.*;
import org.apache.wicket.model.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;
import java.util.*;
import org.apache.wicket.util.convert.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public class CheckGroup<T> extends FormComponent<Collection<T>> implements IOnChangeListener{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    public CheckGroup(final String id){
        super(id);
        this.setRenderBodyOnly(true);
    }
    public CheckGroup(final String id,final Collection<T> collection){
        this(id,(IModel)new CollectionModel(collection));
    }
    public CheckGroup(final String id,final IModel<? extends Collection<T>> model){
        super(id,model);
        this.setRenderBodyOnly(true);
    }
    protected Collection<T> convertValue(final String[] values) throws ConversionException{
        final List<T> collection=(List<T>)Generics.newArrayList();
        if(values!=null&&values.length>0){
            for(final String value : values){
                if(value!=null){
                    final Check<T> checkbox=this.visitChildren((Class<?>)Check.class,(org.apache.wicket.util.visit.IVisitor<Component,Check<T>>)new IVisitor<Check<T>,Check<T>>(){
                        public void component(final Check<T> check,final IVisit<Check<T>> visit){
                            if(String.valueOf(check.getValue()).equals(value)){
                                visit.stop((Object)check);
                            }
                        }
                    });
                    if(checkbox==null){
                        throw new WicketRuntimeException("submitted http post value ["+Strings.join(",",values)+"] for CheckGroup component ["+this.getPath()+"] contains an illegal value ["+value+"] which does not point to a Check component. Due to this the CheckGroup component cannot resolve the selected Check component pointed to by the illegal value. A possible reason is that component hierarchy changed between rendering and form submission.");
                    }
                    collection.add(checkbox.getModelObject());
                }
            }
        }
        return (Collection<T>)collection;
    }
    public void updateModel(){
        FormComponent.updateCollectionModel((FormComponent<Collection<Object>>)this);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.remove("disabled");
        tag.remove("name");
    }
    public final void onSelectionChanged(){
        this.convertInput();
        this.updateModel();
        this.onSelectionChanged((Collection<? extends T>)this.getModelObject());
    }
    protected void onSelectionChanged(final Collection<? extends T> newSelection){
    }
    protected boolean wantOnSelectionChangedNotifications(){
        return false;
    }
    protected boolean getStatelessHint(){
        return !this.wantOnSelectionChangedNotifications()&&super.getStatelessHint();
    }
    static{
        log=LoggerFactory.getLogger(CheckGroup.class);
    }
}
