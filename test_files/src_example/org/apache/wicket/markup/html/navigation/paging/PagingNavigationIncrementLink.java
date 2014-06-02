package org.apache.wicket.markup.html.navigation.paging;

import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.*;

public class PagingNavigationIncrementLink<T> extends Link<T>{
    private static final long serialVersionUID=1L;
    private final int increment;
    protected final IPageable pageable;
    public PagingNavigationIncrementLink(final String id,final IPageable pageable,final int increment){
        super(id);
        this.setAutoEnable(true);
        this.increment=increment;
        this.pageable=pageable;
    }
    public void onClick(){
        this.pageable.setCurrentPage(this.getPageNumber());
        this.setResponsePage(this.getPage());
    }
    public final int getPageNumber(){
        final int idx=this.pageable.getCurrentPage()+this.increment;
        return Math.max(0,Math.min(this.pageable.getPageCount()-1,idx));
    }
    public boolean isFirst(){
        return this.pageable.getCurrentPage()<=0;
    }
    public boolean isLast(){
        return this.pageable.getCurrentPage()>=this.pageable.getPageCount()-1;
    }
    public boolean linksTo(final Page page){
        this.pageable.getCurrentPage();
        return (this.increment<0&&this.isFirst())||(this.increment>0&&this.isLast());
    }
}
