package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.util.collections.*;
import org.apache.wicket.util.string.*;
import java.text.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.*;

public class WicketLinkTagHandler extends AbstractMarkupFilter implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String LINK="link";
    public static final String AUTOLINK_ID="_autolink_";
    private ArrayListStack<Boolean> autolinkStatus;
    private boolean autolinking;
    public WicketLinkTagHandler(){
        super();
        this.autolinking=true;
        this.setAutomaticLinking(Application.get().getMarkupSettings().getAutomaticLinking());
    }
    public void setAutomaticLinking(final boolean enable){
        this.autolinking=enable;
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(this.autolinking&&this.analyzeAutolinkCondition(tag)){
            tag.enableAutolink(true);
            tag.setId("_autolink_");
            tag.setAutoComponentTag(true);
            tag.setModified(true);
            return tag;
        }
        if(tag instanceof WicketTag){
            final WicketTag wtag=(WicketTag)tag;
            if(wtag.isLinkTag()){
                if(tag.isOpen()||tag.isOpenClose()){
                    if(tag.isOpen()){
                        if(this.autolinkStatus==null){
                            this.autolinkStatus=(ArrayListStack<Boolean>)new ArrayListStack();
                        }
                        this.autolinkStatus.push((Object)this.autolinking);
                    }
                    final String autolink=tag.getAttributes().getString("autolink");
                    try{
                        this.autolinking=(Strings.isEmpty((CharSequence)autolink)||Strings.isTrue(autolink));
                    }
                    catch(StringValueConversionException e){
                        throw new WicketRuntimeException("Invalid autolink attribute value \""+autolink+"\"");
                    }
                }
                else if(tag.isClose()){
                    this.autolinking=(boolean)this.autolinkStatus.pop();
                }
                return wtag;
            }
        }
        return tag;
    }
    protected boolean analyzeAutolinkCondition(final ComponentTag tag){
        if(tag.getId()==null){
            final IValueMap attributes=tag.getAttributes();
            String ref=attributes.getString("href");
            if(this.checkRef(ref)){
                return true;
            }
            ref=attributes.getString("src");
            if(this.checkRef(ref)){
                return true;
            }
        }
        return false;
    }
    private final boolean checkRef(final String ref){
        return ref!=null&&!ref.contains((CharSequence)":");
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag instanceof WicketTag){
            final WicketTag wtag=(WicketTag)tag;
            if(wtag.isLinkTag()&&wtag.getNamespace()!=null){
                final String id=tag.getId()+"-"+container.getPage().getAutoIndex();
                return new TransparentWebMarkupContainer(id);
            }
        }
        return null;
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("link");
    }
}
