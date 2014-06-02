package org.apache.wicket.markup.repeater.data;

import java.io.*;
import java.util.*;
import org.apache.wicket.model.*;

public class ListDataProvider<T extends Serializable> implements IDataProvider<T>{
    private static final long serialVersionUID=1L;
    private final List<T> list;
    public ListDataProvider(){
        this(Collections.emptyList());
    }
    public ListDataProvider(final List<T> list){
        super();
        if(list==null){
            throw new IllegalArgumentException("argument [list] cannot be null");
        }
        this.list=list;
    }
    protected List<T> getData(){
        return this.list;
    }
    public Iterator<? extends T> iterator(final int first,final int count){
        final List<T> list=this.getData();
        int toIndex=first+count;
        if(toIndex>list.size()){
            toIndex=list.size();
        }
        return (Iterator<? extends T>)list.subList(first,toIndex).listIterator();
    }
    public int size(){
        return this.getData().size();
    }
    public IModel<T> model(final T object){
        return new Model<T>(object);
    }
    public void detach(){
    }
}
