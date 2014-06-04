package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.convert.*;
import java.util.*;

public class ListMultipleChoice<T> extends AbstractChoice<Collection<T>,T>{
    private static final long serialVersionUID=1L;
    static MetaDataKey<Boolean> RETAIN_DISABLED_META_KEY;
    private static int defaultMaxRows;
    private int maxRows;
    protected static int getDefaultMaxRows(){
        return ListMultipleChoice.defaultMaxRows;
    }
    protected static void setDefaultMaxRows(final int defaultMaxRows){
        ListMultipleChoice.defaultMaxRows=defaultMaxRows;
    }
    public ListMultipleChoice(final String id){
        super(id);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final List<? extends T> choices){
        super(id,choices);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final List<? extends T> choices,final int maxRows){
        super(id,choices);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
        this.maxRows=maxRows;
    }
    public ListMultipleChoice(final String id,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final IModel<? extends Collection<T>> object,final List<? extends T> choices){
        super(id,object,choices);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final IModel<? extends Collection<T>> object,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,object,choices,(IChoiceRenderer<? super Object>)renderer);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final IModel<? extends List<? extends T>> choices){
        super(id,choices);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final IModel<? extends Collection<T>> model,final IModel<? extends List<? extends T>> choices){
        super(id,model,choices);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public ListMultipleChoice(final String id,final IModel<? extends Collection<T>> model,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,(IChoiceRenderer<? super Object>)renderer);
        this.maxRows=ListMultipleChoice.defaultMaxRows;
    }
    public final ListMultipleChoice<T> setMaxRows(final int maxRows){
        this.maxRows=maxRows;
        return this;
    }
    public final String getModelValue(){
        final Collection<T> selectedValues=(Collection<T>)this.getModelObject();
        final AppendingStringBuffer buffer=new AppendingStringBuffer();
        if(selectedValues!=null){
            final List<? extends T> choices=this.getChoices();
            for(final T object : selectedValues){
                final int index=choices.indexOf(object);
                buffer.append(this.getChoiceRenderer().getIdValue((Object)object,index));
                buffer.append(";");
            }
        }
        return buffer.toString();
    }
    protected final boolean isSelected(final T choice,final int index,final String selected){
        if(selected!=null){
            final StringTokenizer tokenizer=new StringTokenizer(selected,";");
            while(tokenizer.hasMoreTokens()){
                final String id=tokenizer.nextToken();
                if(id.equals(this.getChoiceRenderer().getIdValue((Object)choice,index))){
                    return true;
                }
            }
        }
        return false;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.put("multiple",(CharSequence)"multiple");
        if(!tag.getAttributes().containsKey((Object)"size")){
            tag.put("size",Math.min(this.maxRows,this.getChoices().size()));
        }
    }
    protected Collection<T> convertValue(final String[] ids) throws ConversionException{
        if(ids!=null&&ids.length>0&&!Strings.isEmpty((CharSequence)ids[0])){
            return (Collection<T>)this.convertChoiceIdsToChoices(ids);
        }
        final ArrayList<T> result=(ArrayList<T>)new ArrayList();
        this.addRetainedDisabled(result);
        return (Collection<T>)result;
    }
    protected List<T> convertChoiceIdsToChoices(final String[] ids){
        final ArrayList<T> selectedValues=(ArrayList<T>)new ArrayList();
        if(ids!=null&&ids.length>0&&!Strings.isEmpty((CharSequence)ids[0])){
            final Map<String,T> choiceIds2choiceValues=this.createChoicesIdsMap();
            for(final String id : ids){
                if(choiceIds2choiceValues.containsKey(id)){
                    selectedValues.add(choiceIds2choiceValues.get(id));
                }
            }
        }
        this.addRetainedDisabled(selectedValues);
        return (List<T>)selectedValues;
    }
    private Map<String,T> createChoicesIdsMap(){
        final List<? extends T> choices=this.getChoices();
        final Map<String,T> choiceIds2choiceValues=(Map<String,T>)new HashMap(choices.size(),1.0f);
        for(int index=0;index<choices.size();++index){
            final T choice=(T)choices.get(index);
            choiceIds2choiceValues.put(this.getChoiceRenderer().getIdValue((Object)choice,index),choice);
        }
        return choiceIds2choiceValues;
    }
    private void addRetainedDisabled(final ArrayList<T> selectedValues){
        if(this.isRetainDisabledSelected()){
            final Collection<T> unchangedModel=(Collection<T>)this.getModelObject();
            final StringBuilder builder=new StringBuilder();
            for(final T t : unchangedModel){
                builder.append(t);
                builder.append(";");
            }
            final String selected=builder.toString();
            final List<? extends T> choices=this.getChoices();
            for(int i=0;i<choices.size();++i){
                final T choice=(T)choices.get(i);
                if(this.isDisabled(choice,i,selected)&&unchangedModel.contains(choice)&&!selectedValues.contains(choice)){
                    selectedValues.add(choice);
                }
            }
        }
    }
    public void updateModel(){
        FormComponent.updateCollectionModel((FormComponent<Collection<Object>>)this);
    }
    public boolean isRetainDisabledSelected(){
        final Boolean flag=this.getMetaData(ListMultipleChoice.RETAIN_DISABLED_META_KEY);
        return flag!=null&&flag;
    }
    public ListMultipleChoice<T> setRetainDisabledSelected(final boolean retain){
        this.setMetaData(ListMultipleChoice.RETAIN_DISABLED_META_KEY,retain?true:null);
        return this;
    }
    static{
        ListMultipleChoice.RETAIN_DISABLED_META_KEY=new MetaDataKey<Boolean>(){
            private static final long serialVersionUID=1L;
        };
        ListMultipleChoice.defaultMaxRows=8;
    }
}
