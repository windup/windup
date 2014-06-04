package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;

public class RedirectPage extends WebPage{
    private static final long serialVersionUID=1L;
    public RedirectPage(final CharSequence url){
        this(url,0);
    }
    public RedirectPage(final CharSequence url,final int waitBeforeRedirectInSeconds){
        super();
        final WebMarkupContainer redirect=new WebMarkupContainer("redirect");
        final String content=waitBeforeRedirectInSeconds+";URL="+(Object)url;
        redirect.add(new AttributeModifier("content",new Model<Object>(content)));
        this.add(redirect);
    }
    public RedirectPage(final Page page){
        this(page.urlFor(IRedirectListener.INTERFACE,page.getPageParameters()),0);
    }
    public RedirectPage(final Page page,final int waitBeforeRedirectInSeconds){
        this(page.urlFor(IRedirectListener.INTERFACE,page.getPageParameters()),waitBeforeRedirectInSeconds);
    }
    public boolean isVersioned(){
        return false;
    }
}
