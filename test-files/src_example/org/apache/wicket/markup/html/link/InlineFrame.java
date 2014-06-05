package org.apache.wicket.markup.html.link;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.markup.*;

public class InlineFrame extends WebMarkupContainer implements ILinkListener{
    private static final long serialVersionUID=1L;
    private final IPageLink pageLink;
    public InlineFrame(final String id,final Class<C> c){
        this(id,c,null);
    }
    public InlineFrame(final String id,final Class<C> c,final PageParameters params){
        this(id,new IPageLink(){
            private static final long serialVersionUID=1L;
            public Page getPage(){
                if(params==null){
                    return (Page)Session.get().getPageFactory().newPage((Class<IRequestablePage>)c);
                }
                return (Page)Session.get().getPageFactory().newPage((Class<IRequestablePage>)c,params);
            }
            public Class<? extends Page> getPageIdentity(){
                return c;
            }
        });
        if(!Page.class.isAssignableFrom(c)){
            throw new IllegalArgumentException("Class "+c+" is not a subclass of Page");
        }
    }
    public InlineFrame(final String id,final Page page){
        this(id,new IPageLink(){
            private static final long serialVersionUID=1L;
            public Page getPage(){
                return page;
            }
            public Class<? extends Page> getPageIdentity(){
                return (Class<? extends Page>)page.getClass();
            }
        });
    }
    public InlineFrame(final String id,final IPageLink pageLink){
        super(id);
        this.pageLink=pageLink;
    }
    protected CharSequence getURL(){
        return this.urlFor(ILinkListener.INTERFACE,new PageParameters());
    }
    protected final void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"iframe");
        final CharSequence url=this.getURL();
        tag.put("src",url);
        super.onComponentTag(tag);
    }
    public final void onLinkClicked(){
        this.setResponsePage(this.pageLink.getPage());
    }
    protected boolean getStatelessHint(){
        return false;
    }
}
