package org.apache.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.markup.html.navigation.paging.*;
import org.apache.wicket.markup.html.link.*;

public class AjaxPagingNavigation extends PagingNavigation{
    private static final long serialVersionUID=1L;
    public AjaxPagingNavigation(final String id,final IPageable pageable){
        this(id,pageable,null);
    }
    public AjaxPagingNavigation(final String id,final IPageable pageable,final IPagingLabelProvider labelProvider){
        super(id,pageable,labelProvider);
    }
    protected Link<?> newPagingNavigationLink(final String id,final IPageable pageable,final int pageIndex){
        return new AjaxPagingNavigationLink(id,pageable,pageIndex);
    }
}
