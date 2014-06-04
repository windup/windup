package org.apache.wicket.markup.html.link;

import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class BookmarkablePageLink<T> extends Link<T>{
    private static final long serialVersionUID=1L;
    private final String pageClassName;
    protected PageParameters parameters;
    public BookmarkablePageLink(final String id,final Class<C> pageClass){
        this(id,pageClass,null);
    }
    public PageParameters getPageParameters(){
        if(this.parameters==null){
            this.parameters=new PageParameters();
        }
        return this.parameters;
    }
    public BookmarkablePageLink(final String id,final Class<C> pageClass,final PageParameters parameters){
        super(id);
        this.parameters=parameters;
        if(pageClass==null){
            throw new IllegalArgumentException("Page class for bookmarkable link cannot be null");
        }
        if(!Page.class.isAssignableFrom(pageClass)){
            throw new IllegalArgumentException("Page class must be derived from "+Page.class.getName());
        }
        this.pageClassName=pageClass.getName();
    }
    public final Class<? extends Page> getPageClass(){
        return WicketObjects.resolveClass(this.pageClassName);
    }
    public boolean linksTo(final Page page){
        return page.getClass()==this.getPageClass();
    }
    protected boolean getStatelessHint(){
        return true;
    }
    public final void onClick(){
    }
    private void setParameterImpl(final String key,final Object value){
        this.getPageParameters().set(key,value);
    }
    @Deprecated
    public BookmarkablePageLink<T> setParameter(final String property,final int value){
        this.setParameterImpl(property,Integer.toString(value));
        return this;
    }
    @Deprecated
    public BookmarkablePageLink<T> setParameter(final String property,final long value){
        this.setParameterImpl(property,Long.toString(value));
        return this;
    }
    @Deprecated
    public BookmarkablePageLink<T> setParameter(final String property,final String value){
        this.setParameterImpl(property,value);
        return this;
    }
    protected CharSequence getURL(){
        final PageParameters parameters=this.getPageParameters();
        return this.urlFor(this.getPageClass(),parameters);
    }
}
