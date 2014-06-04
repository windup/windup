package org.apache.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.markup.html.navigation.paging.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.link.*;

public class AjaxPagingNavigator extends PagingNavigator{
    private static final long serialVersionUID=1L;
    private final IPageable pageable;
    public AjaxPagingNavigator(final String id,final IPageable pageable){
        this(id,pageable,null);
    }
    public AjaxPagingNavigator(final String id,final IPageable pageable,final IPagingLabelProvider labelProvider){
        super(id,pageable,labelProvider);
        this.pageable=pageable;
        this.setOutputMarkupId(true);
    }
    protected Link<?> newPagingNavigationIncrementLink(final String id,final IPageable pageable,final int increment){
        return new AjaxPagingNavigationIncrementLink(id,pageable,increment);
    }
    protected Link<?> newPagingNavigationLink(final String id,final IPageable pageable,final int pageNumber){
        return new AjaxPagingNavigationLink(id,pageable,pageNumber);
    }
    protected PagingNavigation newNavigation(final String id,final IPageable pageable,final IPagingLabelProvider labelProvider){
        return new AjaxPagingNavigation(id,pageable,labelProvider);
    }
    protected void onAjaxEvent(final AjaxRequestTarget target){
        Component container;
        for(container=(Component)this.pageable;container instanceof AbstractRepeater;container=container.getParent()){
        }
        target.add(container);
        if(!((MarkupContainer)container).contains(this,true)){
            target.add(this);
        }
    }
}
