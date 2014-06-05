package org.apache.wicket.markup.resolver;

import org.apache.wicket.markup.html.internal.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.markup.parser.filter.*;

public class HtmlHeaderResolver implements IComponentResolver{
    private static final long serialVersionUID=1L;
    public static final String HEAD="head";
    public Component resolve(final MarkupContainer container,final MarkupStream markupStream,final ComponentTag tag){
        if(tag.getId().equals("_header_")){
            return this.newHtmlHeaderContainer("_header_"+container.getPage().getAutoIndex());
        }
        if(!(tag instanceof WicketTag)||!((WicketTag)tag).isHeadTag()){
            return null;
        }
        if(container instanceof WebPage){
            final MarkupContainer header=this.newHtmlHeaderContainer("_header_"+container.getPage().getAutoIndex());
            final WebMarkupContainer header2=new TransparentWebMarkupContainer("_header_");
            header2.setRenderBodyOnly(true);
            header.add(header2);
            return header;
        }
        if(container instanceof HtmlHeaderContainer){
            final WebMarkupContainer header3=new TransparentWebMarkupContainer("_header_");
            header3.setRenderBodyOnly(true);
            return header3;
        }
        final Page page=container.getPage();
        final String pageClassName=(page!=null)?page.getClass().getName():"unknown";
        final IResourceStream stream=markupStream.getResource();
        final String streamName=(stream!=null)?stream.toString():"unknown";
        throw new MarkupException("Mis-placed <wicket:head>. <wicket:head> must be outside of <wicket:panel>, <wicket:border>, and <wicket:extend>. Error occured while rendering page: "+pageClassName+" using markup stream: "+streamName);
    }
    protected HtmlHeaderContainer newHtmlHeaderContainer(final String id){
        return new HtmlHeaderContainer(id);
    }
    static{
        WicketTagIdentifier.registerWellKnownTagName("head");
    }
}
