package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.string.*;
import java.text.*;
import java.util.*;

public final class WicketTagIdentifier extends AbstractMarkupFilter{
    private static List<String> wellKnownTagNames;
    public WicketTagIdentifier(final MarkupResourceStream markup){
        super(markup);
    }
    protected MarkupElement onComponentTag(ComponentTag tag) throws ParseException{
        final String namespace=this.getWicketNamespace();
        final String wicketIdValue=tag.getAttributes().getString(namespace+":id");
        if(namespace.equalsIgnoreCase(tag.getNamespace())){
            tag=new WicketTag(tag.getXmlTag());
            if(!this.isWellKnown(tag)){
                throw new WicketParseException("Unknown tag name with Wicket namespace: '"+tag.getName()+"'. Might be you haven't installed the appropriate resolver?",tag);
            }
            if(Strings.isEmpty((CharSequence)wicketIdValue)){
                tag.setId("_wicket_"+tag.getName());
                tag.setAutoComponentTag(true);
                tag.setModified(true);
            }
        }
        if(wicketIdValue!=null){
            if(wicketIdValue.trim().length()==0){
                throw new WicketParseException("The wicket:id attribute value must not be empty. May be unmatched quotes?!?",tag);
            }
            tag.setId(wicketIdValue);
        }
        return tag;
    }
    public static void registerWellKnownTagName(final String name){
        if(WicketTagIdentifier.wellKnownTagNames==null){
            WicketTagIdentifier.wellKnownTagNames=(List<String>)new ArrayList();
        }
        if(!WicketTagIdentifier.wellKnownTagNames.contains(name)){
            WicketTagIdentifier.wellKnownTagNames.add(name);
        }
    }
    private boolean isWellKnown(final ComponentTag tag){
        for(final String name : WicketTagIdentifier.wellKnownTagNames){
            if(tag.getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }
}
