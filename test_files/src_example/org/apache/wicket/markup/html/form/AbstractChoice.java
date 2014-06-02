package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import java.util.*;
import org.apache.wicket.model.util.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.util.convert.*;
import org.apache.wicket.util.string.*;

public abstract class AbstractChoice<T,E> extends FormComponent<T>{
    private static final long serialVersionUID=1L;
    private IModel<? extends List<? extends E>> choices;
    private IChoiceRenderer<? super E> renderer;
    public AbstractChoice(final String id){
        this(id,(IModel)new WildcardListModel(new ArrayList()),(IChoiceRenderer)new ChoiceRenderer());
    }
    public AbstractChoice(final String id,final List<? extends E> choices){
        this(id,(IModel)new WildcardListModel(choices),(IChoiceRenderer)new ChoiceRenderer());
    }
    public AbstractChoice(final String id,final List<? extends E> choices,final IChoiceRenderer<? super E> renderer){
        this(id,(IModel)new WildcardListModel(choices),renderer);
    }
    public AbstractChoice(final String id,final IModel<T> model,final List<? extends E> choices){
        this(id,model,(IModel)new WildcardListModel(choices),(IChoiceRenderer)new ChoiceRenderer());
    }
    public AbstractChoice(final String id,final IModel<T> model,final List<? extends E> choices,final IChoiceRenderer<? super E> renderer){
        this(id,model,(IModel)new WildcardListModel(choices),renderer);
    }
    public AbstractChoice(final String id,final IModel<? extends List<? extends E>> choices){
        this(id,choices,(IChoiceRenderer)new ChoiceRenderer());
    }
    public AbstractChoice(final String id,final IModel<? extends List<? extends E>> choices,final IChoiceRenderer<? super E> renderer){
        super(id);
        this.choices=this.wrap(choices);
        this.setChoiceRenderer(renderer);
    }
    public AbstractChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends E>> choices){
        this(id,model,choices,(IChoiceRenderer)new ChoiceRenderer());
    }
    public AbstractChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends E>> choices,final IChoiceRenderer<? super E> renderer){
        super(id,model);
        this.choices=this.wrap(choices);
        this.setChoiceRenderer(renderer);
    }
    public List<? extends E> getChoices(){
        final List<? extends E> choices=(List<? extends E>)((this.choices!=null)?((List)this.choices.getObject()):null);
        if(choices==null){
            throw new NullPointerException("List of choices is null - Was the supplied 'Choices' model empty?");
        }
        return choices;
    }
    public final AbstractChoice<T,E> setChoices(final IModel<? extends List<? extends E>> choices){
        if(this.choices!=null&&this.choices!=choices&&this.isVersioned()){
            this.addStateChange();
        }
        this.choices=this.wrap(choices);
        return this;
    }
    public final AbstractChoice<T,E> setChoices(final List<E> choices){
        if(this.choices!=null&&this.isVersioned()){
            this.addStateChange();
        }
        this.choices=(IModel<? extends List<? extends E>>)new WildcardListModel(choices);
        return this;
    }
    public final IChoiceRenderer<? super E> getChoiceRenderer(){
        return this.renderer;
    }
    public final AbstractChoice<T,E> setChoiceRenderer(IChoiceRenderer<? super E> renderer){
        if(renderer==null){
            renderer=new ChoiceRenderer<Object>();
        }
        this.renderer=renderer;
        return this;
    }
    protected void detachModel(){
        super.detachModel();
        if(this.choices!=null){
            this.choices.detach();
        }
    }
    protected CharSequence getDefaultChoice(final String selectedValue){
        return (CharSequence)"";
    }
    protected abstract boolean isSelected(final E p0,final int p1,final String p2);
    protected boolean isDisabled(final E object,final int index,final String selected){
        return false;
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        final List<? extends E> choices=this.getChoices();
        final AppendingStringBuffer buffer=new AppendingStringBuffer(choices.size()*50+16);
        final String selectedValue=this.getValue();
        buffer.append((Object)this.getDefaultChoice(selectedValue));
        for(int index=0;index<choices.size();++index){
            final E choice=(E)choices.get(index);
            this.appendOptionHtml(buffer,choice,index,selectedValue);
        }
        buffer.append("\n");
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)buffer);
    }
    protected void appendOptionHtml(final AppendingStringBuffer buffer,final E choice,final int index,final String selected){
        final Object objectValue=this.renderer.getDisplayValue((Object)choice);
        final Class<?> objectClass=(Class<?>)((objectValue==null)?null:objectValue.getClass());
        String displayValue="";
        if(objectClass!=null&&objectClass!=String.class){
            final IConverter converter=this.getConverter(objectClass);
            displayValue=converter.convertToString(objectValue,this.getLocale());
        }
        else if(objectValue!=null){
            displayValue=objectValue.toString();
        }
        buffer.append("\n<option ");
        this.setOptionAttributes(buffer,choice,index,selected);
        buffer.append(">");
        String display=displayValue;
        if(this.localizeDisplayValues()){
            display=this.getLocalizer().getString(displayValue,this,displayValue);
        }
        CharSequence escaped=(CharSequence)display;
        if(this.getEscapeModelStrings()){
            escaped=this.escapeOptionHtml(display);
        }
        buffer.append((Object)escaped);
        buffer.append("</option>");
    }
    protected void setOptionAttributes(final AppendingStringBuffer buffer,final E choice,final int index,final String selected){
        if(this.isSelected(choice,index,selected)){
            buffer.append("selected=\"selected\" ");
        }
        if(this.isDisabled(choice,index,selected)){
            buffer.append("disabled=\"disabled\" ");
        }
        buffer.append("value=\"");
        buffer.append((Object)Strings.escapeMarkup((CharSequence)this.renderer.getIdValue((Object)choice,index)));
        buffer.append("\"");
    }
    protected CharSequence escapeOptionHtml(final String displayValue){
        return Strings.escapeMarkup((CharSequence)displayValue);
    }
    protected boolean localizeDisplayValues(){
        return false;
    }
    public final FormComponent<T> setType(final Class<?> type){
        throw new UnsupportedOperationException("This class does not support type-conversion because it is performed exclusively by the IChoiceRenderer assigned to this component");
    }
}
