package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.*;
import java.text.*;
import org.apache.wicket.*;
import org.apache.wicket.util.value.*;
import java.util.*;

public final class WicketNamespaceHandler extends AbstractMarkupFilter{
    private static final String WICKET_URI="http://wicket.apache.org";
    private static final String XMLNS="xmlns:";
    public WicketNamespaceHandler(final MarkupResourceStream markup){
        super(markup);
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag.isOpen()&&"html".equals(tag.getName().toLowerCase())){
            final String namespace=this.determineWicketNamespace(tag);
            if(namespace!=null){
                this.getMarkupResourceStream().setWicketNamespace(namespace);
            }
        }
        return tag;
    }
    private String determineWicketNamespace(final ComponentTag tag){
        final IValueMap attributes=tag.getAttributes();
        for(final Map.Entry<String,Object> entry : attributes.entrySet()){
            final String attributeName=(String)entry.getKey();
            if(attributeName.startsWith("xmlns:")){
                final String xmlnsUrl=(String)entry.getValue();
                if(xmlnsUrl==null||xmlnsUrl.trim().length()==0||xmlnsUrl.startsWith("http://wicket.apache.org")){
                    final String namespace=attributeName.substring("xmlns:".length());
                    if(Application.get().getMarkupSettings().getStripWicketTags()){
                        attributes.remove((Object)attributeName);
                        tag.setModified(true);
                    }
                    return namespace;
                }
                continue;
            }
        }
        return null;
    }
}
