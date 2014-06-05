package org.apache.wicket.markup.repeater.data;

import java.util.*;
import org.apache.wicket.model.*;

public class EmptyDataProvider<T> implements IDataProvider<T>{
    private static final long serialVersionUID=1L;
    private static EmptyDataProvider<?> INSTANCE;
    public static <T> EmptyDataProvider<T> getInstance(){
        return (EmptyDataProvider<T>)EmptyDataProvider.INSTANCE;
    }
    public Iterator<T> iterator(final int first,final int count){
        final List<T> list=(List<T>)Collections.emptyList();
        return (Iterator<T>)list.iterator();
    }
    public int size(){
        return 0;
    }
    public IModel<T> model(final Object object){
        return null;
    }
    public void detach(){
    }
    static{
        EmptyDataProvider.INSTANCE=new EmptyDataProvider<Object>();
    }
}
