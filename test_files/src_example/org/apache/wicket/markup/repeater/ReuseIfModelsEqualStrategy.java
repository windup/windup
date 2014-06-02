package org.apache.wicket.markup.repeater;

import org.apache.wicket.model.*;
import org.apache.wicket.util.lang.*;
import java.util.*;

public class ReuseIfModelsEqualStrategy implements IItemReuseStrategy{
    private static final long serialVersionUID=1L;
    private static IItemReuseStrategy instance;
    public static IItemReuseStrategy getInstance(){
        return ReuseIfModelsEqualStrategy.instance;
    }
    public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory,final Iterator<IModel<T>> newModels,final Iterator<Item<T>> existingItems){
        final Map<IModel<T>,Item<T>> modelToItem=(Map<IModel<T>,Item<T>>)Generics.newHashMap();
        while(existingItems.hasNext()){
            final Item<T> item=(Item<T>)existingItems.next();
            modelToItem.put(item.getModel(),item);
        }
        return new Iterator<Item<T>>(){
            private int index=0;
            public boolean hasNext(){
                return newModels.hasNext();
            }
            public Item<T> next(){
                final IModel<T> model=(IModel<T>)newModels.next();
                final Item<T> oldItem=(Item<T>)modelToItem.get(model);
                Item<T> item;
                if(oldItem==null){
                    item=factory.newItem(this.index,model);
                }
                else{
                    oldItem.setIndex(this.index);
                    item=oldItem;
                }
                ++this.index;
                return item;
            }
            public void remove(){
                throw new UnsupportedOperationException();
            }
        };
    }
    static{
        ReuseIfModelsEqualStrategy.instance=new ReuseIfModelsEqualStrategy();
    }
}
