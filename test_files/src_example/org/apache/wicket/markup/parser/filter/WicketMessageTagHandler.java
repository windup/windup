package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.resolver.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.util.string.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;
import org.apache.wicket.model.*;

public final class WicketMessageTagHandler extends AbstractMarkupFilter implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String WICKET_MESSAGE_CONTAINER_ID="_message_attr_";
    @Deprecated
    public static final Behavior ATTRIBUTE_LOCALIZER;
    public WicketMessageTagHandler(){
        this(null);
    }
    public WicketMessageTagHandler(final MarkupResourceStream markupResourceStream){
        super(markupResourceStream);
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag.isClose()){
            return tag;
        }
        final String wicketMessageAttribute=tag.getAttributes().getString(this.getWicketMessageAttrName());
        if(!Strings.isEmpty((CharSequence)wicketMessageAttribute)){
            if(tag.getId()==null){
                tag.setId("_message_attr_");
                tag.setAutoComponentTag(true);
                tag.setModified(true);
            }
            tag.addBehavior(new AttributeLocalizer(this.getWicketMessageAttrName()));
        }
        return tag;
    }
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag!=null&&tag.getId().startsWith("_message_attr_")){
            final int autoIndex=container.getPage().getAutoIndex();
            final String id="_message_attr_"+autoIndex;
            Component wc;
            if(tag.isOpenClose()){
                wc=new WebComponent(id);
            }
            else{
                wc=new TransparentWebMarkupContainer(id);
            }
            return wc;
        }
        return null;
    }
    private String getWicketMessageAttrName(){
        final String wicketNamespace=this.getWicketNamespace();
        return wicketNamespace+':'+"message";
    }
    static{
        ATTRIBUTE_LOCALIZER=new AttributeLocalizer();
    }
    public static class AttributeLocalizer extends Behavior{
        private static final long serialVersionUID=1L;
        private final String wicketMessageAttrName;
        public AttributeLocalizer(){
            this("wicket:message");
        }
        public AttributeLocalizer(final String wicketMessageAttrName){
            super();
            this.wicketMessageAttrName=wicketMessageAttrName;
        }
        public void onComponentTag(final Component component,final ComponentTag tag){
            String expr=tag.getAttributes().getString(this.wicketMessageAttrName);
            if(!Strings.isEmpty((CharSequence)expr)){
                expr=expr.trim();
                final String[] arr$;
                final String[] attrsAndKeys=arr$=expr.split(",");
                for(final String attrAndKey : arr$){
                    final int colon=attrAndKey.lastIndexOf(":");
                    if(attrAndKey.length()<3||colon<1||colon>attrAndKey.length()-2){
                        throw new WicketRuntimeException("wicket:message attribute contains an invalid value [["+expr+"]], must be of form (attr:key)+");
                    }
                    final String attr=attrAndKey.substring(0,colon);
                    final String key=attrAndKey.substring(colon+1);
                    String value;
                    if(tag.getAttributes().containsKey((Object)attr)){
                        value=component.getString(key,null,tag.getAttributes().getString(attr));
                    }
                    else{
                        value=component.getString(key);
                    }
                    tag.put(attr,(CharSequence)value);
                }
            }
        }
    }
}
