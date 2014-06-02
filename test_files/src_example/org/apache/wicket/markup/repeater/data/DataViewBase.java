package org.apache.wicket.markup.repeater.data;

import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.model.*;

public abstract class DataViewBase<T> extends AbstractPageableView<T>{
    private static final long serialVersionUID=1L;
    private final IDataProvider<T> dataProvider;
    public DataViewBase(final String id,final IDataProvider<T> dataProvider){
        super(id);
        this.dataProvider=(IDataProvider<T>)Args.notNull((Object)dataProvider,"dataProvider");
    }
    protected final IDataProvider<T> internalGetDataProvider(){
        return this.dataProvider;
    }
    protected final Iterator<IModel<T>> getItemModels(final int offset,final int count){
        return new ModelIterator<T>(this.internalGetDataProvider(),offset,count);
    }
    protected final int internalGetItemCount(){
        return this.internalGetDataProvider().size();
    }
    protected void onDetach(){
        this.dataProvider.detach();
        super.onDetach();
    }
    private static final class ModelIterator<T> implements Iterator<IModel<T>>{
        private final Iterator<? extends T> items;
        private final IDataProvider<T> dataProvider;
        private final int max;
        private int index;
        public ModelIterator(final IDataProvider<T> dataProvider,final int offset,final int count){
            super();
            this.dataProvider=dataProvider;
            this.max=count;
            this.items=((count>0)?dataProvider.iterator(offset,count):null);
        }
        public void remove(){
            throw new UnsupportedOperationException();
        }
        public boolean hasNext(){
            return this.items!=null&&this.items.hasNext()&&this.index<this.max;
        }
        public IModel<T> next(){
            ++this.index;
            return this.dataProvider.model((T)this.items.next());
        }
    }
}
