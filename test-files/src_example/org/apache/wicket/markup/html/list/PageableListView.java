package org.apache.wicket.markup.html.list;

import org.apache.wicket.markup.html.navigation.paging.*;
import org.apache.wicket.model.*;
import java.util.*;

public abstract class PageableListView<T> extends ListView<T> implements IPageableItems{
    private static final long serialVersionUID=1L;
    private int currentPage;
    private int itemsPerPage;
    public PageableListView(final String id,final IModel<? extends List<? extends T>> model,final int itemsPerPage){
        super(id,model);
        this.itemsPerPage=itemsPerPage;
    }
    public PageableListView(final String id,final List<? extends T> list,final int itemsPerPage){
        super(id,list);
        this.itemsPerPage=itemsPerPage;
    }
    public final int getCurrentPage(){
        while(this.currentPage>0&&this.currentPage*this.itemsPerPage>=this.getItemCount()){
            --this.currentPage;
        }
        return this.currentPage;
    }
    public final int getPageCount(){
        return (this.getItemCount()+this.itemsPerPage-1)/this.itemsPerPage;
    }
    public final int getItemsPerPage(){
        return this.itemsPerPage;
    }
    public final void setItemsPerPage(int itemsPerPage){
        if(itemsPerPage<0){
            itemsPerPage=0;
        }
        this.addStateChange();
        this.itemsPerPage=itemsPerPage;
    }
    public int getFirstItemOffset(){
        return this.getCurrentPage()*this.getItemsPerPage();
    }
    public int getItemCount(){
        return this.getList().size();
    }
    public int getViewSize(){
        if(this.getDefaultModelObject()!=null){
            super.setStartIndex(this.getFirstItemOffset());
            super.setViewSize(this.getItemsPerPage());
        }
        return super.getViewSize();
    }
    public final void setCurrentPage(int currentPage){
        if(currentPage<0){
            currentPage=0;
        }
        final int pageCount=this.getPageCount();
        if(currentPage>0&&currentPage>=pageCount){
            currentPage=pageCount-1;
        }
        this.addStateChange();
        this.currentPage=currentPage;
    }
    public ListView<T> setStartIndex(final int startIndex) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("You must not use setStartIndex() with PageableListView");
    }
    public ListView<T> setViewSize(final int size) throws UnsupportedOperationException{
        throw new UnsupportedOperationException("You must not use setViewSize() with PageableListView");
    }
}
