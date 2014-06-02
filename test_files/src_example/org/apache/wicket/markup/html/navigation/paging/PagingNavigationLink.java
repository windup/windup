package org.apache.wicket.markup.html.navigation.paging;

import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.*;

public class PagingNavigationLink<T> extends Link<T>{
    private static final long serialVersionUID=1L;
    protected final IPageable pageable;
    private final int pageNumber;
    public PagingNavigationLink(final String id,final IPageable pageable,final int pageNumber){
        super(id);
        this.setAutoEnable(true);
        this.pageNumber=pageNumber;
        this.pageable=pageable;
    }
    public void onClick(){
        this.pageable.setCurrentPage(this.getPageNumber());
    }
    public final int getPageNumber(){
        return this.cullPageNumber(this.pageNumber);
    }
    protected int cullPageNumber(final int pageNumber){
        int idx=pageNumber;
        if(idx<0){
            idx+=this.pageable.getPageCount();
        }
        if(idx>this.pageable.getPageCount()-1){
            idx=this.pageable.getPageCount()-1;
        }
        if(idx<0){
            idx=0;
        }
        return idx;
    }
    public final boolean isFirst(){
        return this.getPageNumber()==0;
    }
    public final boolean isLast(){
        return this.getPageNumber()==this.pageable.getPageCount()-1;
    }
    public final boolean linksTo(final Page page){
        return this.getPageNumber()==this.pageable.getCurrentPage();
    }
}
