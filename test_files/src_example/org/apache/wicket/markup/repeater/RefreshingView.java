package org.apache.wicket.markup.repeater;

import org.apache.wicket.model.*;
import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;

public abstract class RefreshingView<T> extends RepeatingView{
    private static final long serialVersionUID=1L;
    private IItemReuseStrategy itemReuseStrategy;
    public RefreshingView(final String id){
        super(id);
    }
    public RefreshingView(final String id,final IModel<?> model){
        super(id,model);
    }
    protected final void onPopulate(){
        final Iterator<IModel<T>> models=this.getItemModels();
        final Iterator<Item<T>> items=this.getItemReuseStrategy().getItems(this.newItemFactory(),models,this.getItems());
        this.removeAll();
        this.addItems(items);
    }
    protected IItemFactory<T> newItemFactory(){
        return new IItemFactory<T>(){
            public Item<T> newItem(final int index,final IModel<T> model){
                final String id=RefreshingView.this.newChildId();
                final Item<T> item=RefreshingView.this.newItem(id,index,model);
                RefreshingView.this.populateItem(item);
                return item;
            }
        };
    }
    protected abstract Iterator<IModel<T>> getItemModels();
    protected abstract void populateItem(final Item<T> p0);
    protected Item<T> newItem(final String id,final int index,final IModel<T> model){
        return new Item<T>(id,index,model);
    }
    public Iterator<Item<T>> getItems(){
        return (Iterator<Item<T>>)Generics.iterator((Iterator)this.iterator());
    }
    protected void addItems(final Iterator<Item<T>> items){
        int index=0;
        while(items.hasNext()){
            final Item<T> item=(Item<T>)items.next();
            item.setIndex(index);
            this.add(item);
            ++index;
        }
    }
    public IItemReuseStrategy getItemReuseStrategy(){
        if(this.itemReuseStrategy==null){
            return DefaultItemReuseStrategy.getInstance();
        }
        return this.itemReuseStrategy;
    }
    public RefreshingView<T> setItemReuseStrategy(final IItemReuseStrategy strategy){
        if(strategy==null){
            throw new IllegalArgumentException();
        }
        if(!strategy.equals(this.itemReuseStrategy)){
            if(this.isVersioned()){
                this.addStateChange();
            }
            this.itemReuseStrategy=strategy;
        }
        return this;
    }
}
