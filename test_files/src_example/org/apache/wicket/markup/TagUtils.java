package org.apache.wicket.markup;

import org.apache.wicket.*;
import org.apache.wicket.util.resource.*;
import java.util.*;
import org.apache.wicket.util.value.*;

public class TagUtils{
    private static final String DEFAULT_ATTRIBUTE_SEPARATOR="; ";
    public static final IValueMap ATTRIBUTES_SEPARATORS;
    public static final boolean isBodyTag(final ComponentTag tag){
        return "body".equalsIgnoreCase(tag.getName())&&tag.getNamespace()==null;
    }
    public static final boolean isHeadTag(final MarkupElement elem){
        if(elem instanceof ComponentTag){
            final ComponentTag tag=(ComponentTag)elem;
            if("head".equalsIgnoreCase(tag.getName())&&tag.getNamespace()==null){
                return true;
            }
        }
        return false;
    }
    public static final boolean isWicketTag(final IMarkupFragment markup,final int i){
        final MarkupElement elem=markup.get(i);
        return elem instanceof WicketTag;
    }
    public static final boolean isExtendTag(final IMarkupFragment markup,final int i){
        final MarkupElement elem=markup.get(i);
        if(elem instanceof WicketTag){
            final WicketTag wtag=(WicketTag)elem;
            return wtag.isExtendTag();
        }
        return false;
    }
    public static final boolean isWicketHeadTag(final MarkupElement elem){
        if(elem instanceof WicketTag){
            final WicketTag wtag=(WicketTag)elem;
            if(wtag.isHeadTag()){
                return true;
            }
        }
        return false;
    }
    public static final boolean isWicketBodyTag(final MarkupElement elem){
        if(elem instanceof WicketTag){
            final WicketTag wtag=(WicketTag)elem;
            if(wtag.isBodyTag()){
                return true;
            }
        }
        return false;
    }
    public static final boolean isWicketBorderTag(final MarkupElement elem){
        if(elem instanceof WicketTag){
            final WicketTag wtag=(WicketTag)elem;
            if(wtag.isBorderTag()){
                return true;
            }
        }
        return false;
    }
    public static void copyAttributes(final Component component,final ComponentTag tag){
        final IMarkupFragment markup=((MarkupContainer)component).getMarkup(null);
        final String namespace=markup.getMarkupResourceStream().getWicketNamespace()+":";
        final MarkupElement elem=markup.get(0);
        if(elem instanceof ComponentTag){
            final ComponentTag panelTag=(ComponentTag)elem;
            for(final String key : panelTag.getAttributes().keySet()){
                if(!key.startsWith(namespace)){
                    final String separator=TagUtils.ATTRIBUTES_SEPARATORS.getString(key,"; ");
                    tag.append(key,(CharSequence)panelTag.getAttribute(key),separator);
                }
            }
            return;
        }
        throw new MarkupException((IResourceStream)markup.getMarkupResourceStream(),"Expected a Tag but found raw markup: "+elem.toString());
    }
    static{
        (ATTRIBUTES_SEPARATORS=(IValueMap)new ValueMap()).put((Object)"class",(Object)" ");
        TagUtils.ATTRIBUTES_SEPARATORS.put((Object)"style",(Object)"; ");
        TagUtils.ATTRIBUTES_SEPARATORS.put((Object)"onclick",(Object)"; ");
    }
}
