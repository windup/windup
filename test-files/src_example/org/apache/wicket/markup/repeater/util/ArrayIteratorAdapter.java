package org.apache.wicket.markup.repeater.util;

import java.util.*;
import org.apache.wicket.model.*;

public abstract class ArrayIteratorAdapter<T> implements Iterator<IModel<T>>{
    private final T[] array;
    private int pos;
    public ArrayIteratorAdapter(final T[] array){
        super();
        this.pos=0;
        this.array=array;
    }
    public void remove(){
        throw new UnsupportedOperationException("remove() is not allowed");
    }
    public boolean hasNext(){
        return this.pos<this.array.length;
    }
    public IModel<T> next(){
        return (IModel<T>)this.model(this.array[this.pos++]);
    }
    public void reset(){
        this.pos=0;
    }
    protected abstract IModel<T> model(final T p0);
}
