package org.apache.wicket.markup.html.form;

import java.util.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public abstract class AbstractSingleSelectChoice<T> extends AbstractChoice<T,T>{
    private static final long serialVersionUID=1L;
    private static final Logger logger;
    private static final String CHOOSE_ONE="Choose One";
    private boolean nullValid;
    public AbstractSingleSelectChoice(final String id){
        super(id);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final List<? extends T> choices){
        super(id,choices);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final IModel<T> model,final List<? extends T> choices){
        super(id,model,choices);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final IModel<T> model,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,(IChoiceRenderer<? super Object>)renderer);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final IModel<? extends List<? extends T>> choices){
        super(id,choices);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices){
        super(id,model,choices);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.nullValid=false;
    }
    public AbstractSingleSelectChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,(IChoiceRenderer<? super Object>)renderer);
        this.nullValid=false;
    }
    public String getModelValue(){
        final T object=(T)this.getModelObject();
        if(object!=null){
            final int index=this.getChoices().indexOf(object);
            return this.getChoiceRenderer().getIdValue((Object)object,index);
        }
        return "";
    }
    public boolean isNullValid(){
        return this.nullValid;
    }
    public AbstractSingleSelectChoice<T> setNullValid(final boolean nullValid){
        this.nullValid=nullValid;
        return this;
    }
    protected final T convertValue(final String[] value){
        final String tmp=(value!=null&&value.length>0)?value[0]:null;
        return this.convertChoiceIdToChoice(tmp);
    }
    protected T convertChoiceIdToChoice(final String id){
        final List<? extends T> choices=this.getChoices();
        final IChoiceRenderer<? super T> renderer=this.getChoiceRenderer();
        for(int index=0;index<choices.size();++index){
            final T choice=(T)choices.get(index);
            if(renderer.getIdValue((Object)choice,index).equals(id)){
                return choice;
            }
        }
        return null;
    }
    protected CharSequence getDefaultChoice(final String selectedValue){
        if(this.isNullValid()){
            String option=this.getLocalizer().getStringIgnoreSettings(this.getNullValidKey(),this,null,null);
            if(Strings.isEmpty((CharSequence)option)){
                option=this.getLocalizer().getString("nullValid",this,"");
            }
            final AppendingStringBuffer buffer=new AppendingStringBuffer(64+option.length());
            buffer.append("\n<option");
            if("".equals(selectedValue)){
                buffer.append(" selected=\"selected\"");
            }
            buffer.append(" value=\"\">").append(option).append("</option>");
            return (CharSequence)buffer;
        }
        if("".equals(selectedValue)){
            String option=this.getLocalizer().getStringIgnoreSettings(this.getNullKey(),this,null,null);
            if(Strings.isEmpty((CharSequence)option)){
                option=this.getLocalizer().getString("null",this,"Choose One");
            }
            return (CharSequence)("\n<option selected=\"selected\" value=\"\">"+option+"</option>");
        }
        return (CharSequence)"";
    }
    protected String getNullValidKey(){
        return this.getId()+".nullValid";
    }
    protected String getNullKey(){
        return this.getId()+".null";
    }
    protected boolean isSelected(final T object,final int index,final String selected){
        return selected!=null&&selected.equals(this.getChoiceRenderer().getIdValue((Object)object,index));
    }
    static{
        logger=LoggerFactory.getLogger(AbstractSingleSelectChoice.class);
    }
}
