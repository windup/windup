package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.*;
import java.text.*;
import org.apache.wicket.markup.parser.*;

public final class HtmlHeaderSectionHandler extends AbstractMarkupFilter{
    private static final String BODY="body";
    private static final String HEAD="head";
    public static final String HEADER_ID="_header_";
    private boolean foundHead;
    private boolean foundClosingHead;
    private boolean ignoreTheRest;
    private final Markup markup;
    public HtmlHeaderSectionHandler(final Markup markup){
        super();
        this.foundHead=false;
        this.foundClosingHead=false;
        this.ignoreTheRest=false;
        this.markup=markup;
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(this.ignoreTheRest){
            return tag;
        }
        if("head".equalsIgnoreCase(tag.getName())){
            if(tag.getNamespace()==null){
                if(tag.isOpen()){
                    this.foundHead=true;
                    if(tag.getId()==null){
                        tag.setId("_header_");
                        tag.setAutoComponentTag(true);
                        tag.setModified(true);
                    }
                }
                else if(tag.isClose()){
                    this.foundClosingHead=true;
                }
                return tag;
            }
            this.foundHead=true;
            this.foundClosingHead=true;
        }
        else if("body".equalsIgnoreCase(tag.getName())&&tag.getNamespace()==null){
            if(this.foundHead&&!this.foundClosingHead){
                throw new MarkupException(new MarkupStream(this.markup),"Invalid page markup. Tag <BODY> found inside <HEAD>");
            }
            if(!this.foundHead){
                this.insertHeadTag();
            }
            this.ignoreTheRest=true;
            return tag;
        }
        return tag;
    }
    private void insertHeadTag(){
        final ComponentTag openTag=new ComponentTag("head",XmlTag.TagType.OPEN);
        openTag.setId("_header_");
        openTag.setAutoComponentTag(true);
        openTag.setModified(true);
        final ComponentTag closeTag=new ComponentTag("head",XmlTag.TagType.CLOSE);
        closeTag.setOpenTag(openTag);
        closeTag.setModified(true);
        this.markup.addMarkupElement(openTag);
        this.markup.addMarkupElement(closeTag);
    }
}
