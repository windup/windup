package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import java.util.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.convert.*;

public class CheckBoxMultipleChoice<T> extends ListMultipleChoice<T>{
    private static final long serialVersionUID=1L;
    private String prefix;
    private String suffix;
    public CheckBoxMultipleChoice(final String id){
        super(id);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final List<? extends T> choices){
        super(id,choices);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final IModel<? extends Collection<T>> model,final List<? extends T> choices){
        super(id,model,choices);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final IModel<? extends Collection<T>> model,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,renderer);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final IModel<? extends List<? extends T>> choices){
        super(id,choices);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final IModel<? extends Collection<T>> model,final IModel<? extends List<? extends T>> choices){
        super(id,model,choices);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public CheckBoxMultipleChoice(final String id,final IModel<? extends Collection<T>> model,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,renderer);
        this.prefix="";
        this.suffix="<br/>\n";
    }
    public String getPrefix(){
        return this.prefix;
    }
    protected String getPrefix(final int index,final T choice){
        return this.getPrefix();
    }
    protected String getSuffix(final int index,final T choice){
        return this.getSuffix();
    }
    public final CheckBoxMultipleChoice<T> setPrefix(final String prefix){
        final Page page=this.findPage();
        if(page!=null){
            this.addStateChange();
        }
        this.prefix=prefix;
        return this;
    }
    public String getSuffix(){
        return this.suffix;
    }
    public final CheckBoxMultipleChoice<T> setSuffix(final String suffix){
        final Page page=this.findPage();
        if(page!=null){
            this.addStateChange();
        }
        this.suffix=suffix;
        return this;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.remove("multiple");
        tag.remove("size");
        tag.remove("disabled");
        tag.remove("name");
    }
    public final void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        final List<? extends T> choices=(List<? extends T>)this.getChoices();
        final AppendingStringBuffer buffer=new AppendingStringBuffer(70*(choices.size()+1));
        final String selected=this.getValue();
        for(int index=0;index<choices.size();++index){
            final T choice=(T)choices.get(index);
            this.appendOptionHtml(buffer,choice,index,selected);
        }
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)buffer);
    }
    protected void appendOptionHtml(final AppendingStringBuffer buffer,final T choice,final int index,final String selected){
        final Object displayValue=this.getChoiceRenderer().getDisplayValue((Object)choice);
        final Class<?> objectClass=(Class<?>)((displayValue==null)?null:displayValue.getClass());
        String label="";
        if(objectClass!=null&&objectClass!=String.class){
            final IConverter converter=this.getConverter(objectClass);
            label=converter.convertToString(displayValue,this.getLocale());
        }
        else if(displayValue!=null){
            label=displayValue.toString();
        }
        if(label!=null){
            buffer.append(this.getPrefix(index,choice));
            final String id=this.getChoiceRenderer().getIdValue((Object)choice,index);
            final String idAttr=this.getCheckBoxMarkupId(id);
            buffer.append("<input name=\"");
            buffer.append(this.getInputName());
            buffer.append("\"");
            buffer.append(" type=\"checkbox\"");
            if(this.isSelected(choice,index,selected)){
                buffer.append(" checked=\"checked\"");
            }
            if(this.isDisabled((T)choice,index,selected)||!this.isEnabledInHierarchy()){
                buffer.append(" disabled=\"disabled\"");
            }
            buffer.append(" value=\"");
            buffer.append(id);
            buffer.append("\" id=\"");
            buffer.append(idAttr);
            buffer.append("\"/>");
            String display=label;
            if(this.localizeDisplayValues()){
                display=this.getLocalizer().getString(label,this,label);
            }
            final CharSequence escaped=(CharSequence)(this.getEscapeModelStrings()?Strings.escapeMarkup((CharSequence)display):display);
            buffer.append("<label for=\"");
            buffer.append(idAttr);
            buffer.append("\">").append((Object)escaped).append("</label>");
            buffer.append(this.getSuffix(index,choice));
        }
    }
    protected String getCheckBoxMarkupId(final String id){
        return this.getMarkupId()+"-"+this.getInputName()+"_"+id;
    }
}
