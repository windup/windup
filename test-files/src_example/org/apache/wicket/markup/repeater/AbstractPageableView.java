package org.apache.wicket.markup.repeater;

import org.apache.wicket.markup.html.navigation.paging.*;
import org.apache.wicket.model.*;
import java.io.*;
import java.util.*;

public abstract class AbstractPageableView<T> extends RefreshingView<T> implements IPageableItems{
    private static final long serialVersionUID=1L;
    private int itemsPerPage;
    private int currentPage;
    private transient int cachedItemCount;
    public AbstractPageableView(final String id,final IModel<? extends Collection<? extends T>> model){
        super(id,model);
        this.itemsPerPage=Integer.MAX_VALUE;
        this.clearCachedItemCount();
    }
    public AbstractPageableView(final String id){
        super(id);
        this.itemsPerPage=Integer.MAX_VALUE;
        this.clearCachedItemCount();
    }
    protected Iterator<IModel<T>> getItemModels(){
        final int offset=this.getFirstItemOffset();
        final int size=this.getViewSize();
        Iterator<IModel<T>> models=this.getItemModels(offset,size);
        models=new CappedIteratorAdapter<T>(models,size);
        return models;
    }
    protected void onBeforeRender(){
        this.clearCachedItemCount();
        super.onBeforeRender();
    }
    protected abstract Iterator<IModel<T>> getItemModels(final int p0,final int p1);
    private void clearCachedItemCount(){
        this.cachedItemCount=-1;
    }
    private void setCachedItemCount(final int itemCount){
        this.cachedItemCount=itemCount;
    }
    private int getCachedItemCount(){
        if(this.cachedItemCount<0){
            throw new IllegalStateException("getItemCountCache() called when cache was not set");
        }
        return this.cachedItemCount;
    }
    private boolean isItemCountCached(){
        return this.cachedItemCount>=0;
    }
    public int getItemsPerPage(){
        return this.itemsPerPage;
    }
    public final void setItemsPerPage(final int items){
        if(items<1){
            throw new IllegalArgumentException("Argument [itemsPerPage] cannot be less than 1");
        }
        if(this.itemsPerPage!=items&&this.isVersioned()){
            this.addStateChange();
        }
        this.itemsPerPage=items;
        this.setCurrentPage(0);
    }
    protected abstract int internalGetItemCount();
    public final int getRowCount(){
        if(!this.isVisibleInHierarchy()){
            return 0;
        }
        return this.getItemCount();
    }
    public final int getItemCount(){
        if(this.isItemCountCached()){
            return this.getCachedItemCount();
        }
        final int count=this.internalGetItemCount();
        this.setCachedItemCount(count);
        return count;
    }
    public final int getCurrentPage(){
        int page=this.currentPage;
        if(page>0&&page>=this.getPageCount()){
            page=Math.max(this.getPageCount()-1,0);
            return this.currentPage=page;
        }
        return page;
    }
    public final void setCurrentPage(final int page){
        if(this.currentPage!=page&&this.isVersioned()){
            this.addStateChange();
        }
        this.currentPage=page;
    }
    public final int getPageCount(){
        final int total=this.getRowCount();
        final int itemsPerPage=this.getItemsPerPage();
        int count=total/itemsPerPage;
        if(itemsPerPage*count<total){
            ++count;
        }
        return count;
    }
    public int getFirstItemOffset(){
        return this.getCurrentPage()*this.getItemsPerPage();
    }
    public int getViewSize(){
        return Math.min(this.getItemsPerPage(),this.getRowCount()-this.getFirstItemOffset());
    }
    protected void onDetach(){
        this.clearCachedItemCount();
        super.onDetach();
    }
    private void readObject(final ObjectInputStream s) throws IOException,ClassNotFoundException{
        s.defaultReadObject();
        this.clearCachedItemCount();
    }
    private static class CappedIteratorAdapter<T> implements Iterator<IModel<T>>{
        private final int max;
        private int index;
        private final Iterator<IModel<T>> delegate;
        public CappedIteratorAdapter(final Iterator<IModel<T>> delegate,final int max){
            super();
            this.delegate=delegate;
            this.max=max;
        }
        public void remove(){
            throw new UnsupportedOperationException();
        }
        public boolean hasNext(){
            return this.index<this.max&&this.delegate.hasNext();
        }
        public IModel<T> next(){
            if(this.index>=this.max){
                throw new NoSuchElementException();
            }
            ++this.index;
            return (IModel<T>)this.delegate.next();
        }
    }
}
