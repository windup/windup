package org.apache.wicket.markup.repeater;

import java.util.*;
import org.apache.wicket.model.*;

public class DefaultItemReuseStrategy implements IItemReuseStrategy{
    private static final long serialVersionUID=1L;
    private static final IItemReuseStrategy instance;
    public static IItemReuseStrategy getInstance(){
        return DefaultItemReuseStrategy.instance;
    }
    public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory,final Iterator<IModel<T>> newModels,final Iterator<Item<T>> existingItems){
        return new Iterator<Item<T>>(){
            private int index=0;
            public void remove(){
                throw new UnsupportedOperationException();
            }
            public boolean hasNext(){
                return newModels.hasNext();
            }
            public Item<T> next(){
                final IModel<T> model=(IModel<T>)newModels.next();
                final Item<T> item=factory.newItem(this.index,model);
                ++this.index;
                return item;
            }
        };
    }
    static{
        instance=new DefaultItemReuseStrategy();
    }
}
