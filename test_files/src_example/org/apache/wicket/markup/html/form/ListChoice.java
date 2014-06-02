package org.apache.wicket.markup.html.form;

import java.util.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;

public class ListChoice<T> extends DropDownChoice<T>{
    private static final long serialVersionUID=1L;
    private static int defaultMaxRows;
    private int maxRows;
    protected static int getDefaultMaxRows(){
        return ListChoice.defaultMaxRows;
    }
    protected static void setDefaultMaxRows(final int defaultMaxRows){
        ListChoice.defaultMaxRows=defaultMaxRows;
    }
    public ListChoice(final String id){
        this(id,null,(List)null,null,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final List<? extends T> choices){
        this(id,null,choices,null,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        this(id,null,choices,renderer,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final IModel<T> model,final List<? extends T> choices){
        this(id,model,choices,null,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final IModel<T> model,final List<? extends T> choices,final int maxRows){
        this(id,model,choices,null,maxRows);
    }
    public ListChoice(final String id,final IModel<T> model,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        this(id,model,choices,renderer,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final IModel<T> model,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer,final int maxRows){
        super(id,model,choices,renderer);
        this.maxRows=maxRows;
    }
    public ListChoice(final String id,final IModel<? extends List<? extends T>> choices){
        this(id,null,choices,null,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices){
        this(id,model,choices,null,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        this(id,null,choices,renderer,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        this(id,model,choices,renderer,ListChoice.defaultMaxRows);
    }
    public ListChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer,final int maxRows){
        super(id,model,choices,renderer);
        this.maxRows=maxRows;
    }
    public final int getMaxRows(){
        return this.maxRows;
    }
    public final ListChoice<T> setMaxRows(final int maxRows){
        this.maxRows=maxRows;
        return this;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(!tag.getAttributes().containsKey((Object)"size")){
            tag.put("size",this.maxRows);
        }
    }
    static{
        ListChoice.defaultMaxRows=8;
    }
}
