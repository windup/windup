package org.apache.wicket.markup.html.navigation.paging;

import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.*;

public class PagingNavigator extends Panel{
    private static final long serialVersionUID=1L;
    private PagingNavigation pagingNavigation;
    private final IPageable pageable;
    private final IPagingLabelProvider labelProvider;
    public PagingNavigator(final String id,final IPageable pageable){
        this(id,pageable,null);
    }
    public PagingNavigator(final String id,final IPageable pageable,final IPagingLabelProvider labelProvider){
        super(id);
        this.pageable=pageable;
        this.labelProvider=labelProvider;
    }
    public final IPageable getPageable(){
        return this.pageable;
    }
    protected void onInitialize(){
        super.onInitialize();
        this.pagingNavigation=this.newNavigation("navigation",this.pageable,this.labelProvider);
        this.add(this.pagingNavigation);
        this.add(this.newPagingNavigationLink("first",this.pageable,0).add(new TitleAppender("PagingNavigator.first")));
        this.add(this.newPagingNavigationIncrementLink("prev",this.pageable,-1).add(new TitleAppender("PagingNavigator.previous")));
        this.add(this.newPagingNavigationIncrementLink("next",this.pageable,1).add(new TitleAppender("PagingNavigator.next")));
        this.add(this.newPagingNavigationLink("last",this.pageable,-1).add(new TitleAppender("PagingNavigator.last")));
    }
    protected AbstractLink newPagingNavigationIncrementLink(final String id,final IPageable pageable,final int increment){
        return new PagingNavigationIncrementLink<Object>(id,pageable,increment);
    }
    protected AbstractLink newPagingNavigationLink(final String id,final IPageable pageable,final int pageNumber){
        return new PagingNavigationLink<Object>(id,pageable,pageNumber);
    }
    protected PagingNavigation newNavigation(final String id,final IPageable pageable,final IPagingLabelProvider labelProvider){
        return new PagingNavigation(id,pageable,labelProvider);
    }
    public final PagingNavigation getPagingNavigation(){
        return this.pagingNavigation;
    }
    private final class TitleAppender extends Behavior{
        private static final long serialVersionUID=1L;
        private final String resourceKey;
        public TitleAppender(final String resourceKey){
            super();
            this.resourceKey=resourceKey;
        }
        public void onComponentTag(final Component component,final ComponentTag tag){
            tag.put("title",(CharSequence)PagingNavigator.this.getString(this.resourceKey));
        }
    }
}
