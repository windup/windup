package org.apache.wicket.markup.repeater;

import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.model.*;
import java.util.*;
import java.io.*;

public class Item<T> extends ListItem<T>{
    private static final long serialVersionUID=1L;
    public Item(final String id,final int index,final IModel<T> model){
        super(id,index,model);
    }
    public Item(final String id,final int index){
        super(id,index);
    }
    public String getPrimaryKey(){
        return this.getId();
    }
    public static class IndexComparator implements Comparator<Item<?>>,Serializable{
        private static final long serialVersionUID=1L;
        private static final Comparator<Item<?>> instance;
        public static final Comparator<Item<?>> getInstance(){
            return IndexComparator.instance;
        }
        public int compare(final Item<?> lhs,final Item<?> rhs){
            return lhs.getIndex()-rhs.getIndex();
        }
        static{
            instance=new IndexComparator();
        }
    }
}
