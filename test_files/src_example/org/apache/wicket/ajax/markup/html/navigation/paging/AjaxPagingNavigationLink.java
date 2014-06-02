package org.apache.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.ajax.markup.html.*;
import org.apache.wicket.markup.html.navigation.paging.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.*;

public class AjaxPagingNavigationLink extends PagingNavigationLink<Void> implements IAjaxLink{
    private static final long serialVersionUID=1L;
    public AjaxPagingNavigationLink(final String id,final IPageable pageable,final int pageNumber){
        super(id,pageable,pageNumber);
        this.setOutputMarkupId(true);
    }
    protected void onInitialize(){
        super.onInitialize();
        this.add(this.newAjaxPagingNavigationBehavior(this.pageable,"onclick"));
    }
    protected AjaxPagingNavigationBehavior newAjaxPagingNavigationBehavior(final IPageable pageable,final String event){
        return new AjaxPagingNavigationBehavior(this,pageable,event);
    }
    public void onClick(){
        this.onClick(null);
    }
    public void onClick(final AjaxRequestTarget target){
        this.pageable.setCurrentPage(this.getPageNumber());
    }
}
