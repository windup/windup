package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.convert.*;
import org.apache.wicket.util.value.*;
import java.util.*;

public class RadioChoice<T> extends AbstractSingleSelectChoice<T> implements IOnChangeListener{
    private static final long serialVersionUID=1L;
    private String prefix;
    private String suffix;
    public RadioChoice(final String id){
        super(id);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final List<? extends T> choices){
        super(id,choices);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final IModel<T> model,final List<? extends T> choices){
        super(id,model,choices);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final IModel<T> model,final List<? extends T> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,renderer);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final IModel<? extends List<? extends T>> choices){
        super(id,choices);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices){
        super(id,model,choices);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,choices,renderer);
        this.prefix="";
        this.suffix="<br />\n";
    }
    public RadioChoice(final String id,final IModel<T> model,final IModel<? extends List<? extends T>> choices,final IChoiceRenderer<? super T> renderer){
        super(id,model,choices,renderer);
        this.prefix="";
        this.suffix="<br />\n";
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.remove("name");
    }
    public void onSelectionChanged(){
        this.convertInput();
        this.updateModel();
        this.onSelectionChanged(this.getDefaultModelObject());
    }
    protected void onSelectionChanged(final Object newSelection){
    }
    protected boolean wantOnSelectionChangedNotifications(){
        return false;
    }
    protected boolean getStatelessHint(){
        return !this.wantOnSelectionChangedNotifications()&&super.getStatelessHint();
    }
    public String getPrefix(){
        return this.prefix;
    }
    public final RadioChoice<T> setPrefix(final String prefix){
        this.addStateChange();
        this.prefix=prefix;
        return this;
    }
    public String getSuffix(){
        return this.suffix;
    }
    public final RadioChoice<T> setSuffix(final String suffix){
        this.addStateChange();
        this.suffix=suffix;
        return this;
    }
    public final void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        final List<? extends T> choices=(List<? extends T>)this.getChoices();
        final AppendingStringBuffer buffer=new AppendingStringBuffer((choices.size()+1)*70);
        final String selected=this.getValue();
        for(int index=0;index<choices.size();++index){
            final T choice=(T)choices.get(index);
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
                buffer.append(this.getPrefix());
                final String id=this.getChoiceRenderer().getIdValue((Object)choice,index);
                final String idAttr=this.getMarkupId()+"-"+id;
                final boolean enabled=this.isEnabledInHierarchy()&&!this.isDisabled((T)choice,index,selected);
                buffer.append("<input name=\"").append(this.getInputName()).append("\"").append(" type=\"radio\"").append(this.isSelected(choice,index,selected)?" checked=\"checked\"":"").append(enabled?"":" disabled=\"disabled\"").append(" value=\"").append(id).append("\" id=\"").append(idAttr).append("\"");
                if(this.wantOnSelectionChangedNotifications()){
                    final CharSequence url=this.urlFor(IOnChangeListener.INTERFACE,new PageParameters());
                    final Form<?> form=this.findParent((Class<Form<?>>)Form.class);
                    if(form!=null){
                        buffer.append(" onclick=\"").append((Object)form.getJsForInterfaceUrl(url)).append(";\"");
                    }
                    else{
                        buffer.append(" onclick=\"window.location.href='").append((Object)url).append(((url.toString().indexOf(63)>-1)?"&":"?")+this.getInputName()).append("=").append(id).append("';\"");
                    }
                }
                final IValueMap attrs=this.getAdditionalAttributes(index,choice);
                if(attrs!=null){
                    for(final Map.Entry<String,Object> attr : attrs.entrySet()){
                        buffer.append(" ").append((String)attr.getKey()).append("=\"").append(attr.getValue()).append("\"");
                    }
                }
                if(this.getApplication().getDebugSettings().isOutputComponentPath()){
                    String path=this.getPageRelativePath();
                    path=path.replace((CharSequence)"_",(CharSequence)"__");
                    path=path.replace((CharSequence)":",(CharSequence)"_");
                    buffer.append(" wicketpath=\"").append(path).append("_input_").append(index).append("\"");
                }
                buffer.append("/>");
                String display=label;
                if(this.localizeDisplayValues()){
                    display=this.getLocalizer().getString(label,this,label);
                }
                CharSequence escaped=(CharSequence)display;
                if(this.getEscapeModelStrings()){
                    escaped=Strings.escapeMarkup((CharSequence)display);
                }
                buffer.append("<label for=\"").append(idAttr).append("\">").append((Object)escaped).append("</label>");
                buffer.append(this.getSuffix());
            }
        }
        this.replaceComponentTagBody(markupStream,openTag,(CharSequence)buffer);
    }
    protected IValueMap getAdditionalAttributes(final int index,final T choice){
        return null;
    }
}
