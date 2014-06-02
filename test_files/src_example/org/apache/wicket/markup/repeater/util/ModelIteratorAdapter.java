package org.apache.wicket.markup.repeater.util;

import java.util.*;
import org.apache.wicket.model.*;

public abstract class ModelIteratorAdapter<T> implements Iterator<IModel<T>>{
    private final Iterator<T> delegate;
    public ModelIteratorAdapter(final Iterator<T> delegate){
        super();
        this.delegate=delegate;
    }
    public boolean hasNext(){
        return this.delegate.hasNext();
    }
    public IModel<T> next(){
        return (IModel<T>)this.model(this.delegate.next());
    }
    public void remove(){
        this.delegate.remove();
    }
    protected abstract IModel<T> model(final T p0);
}
