package org.apache.wicket.util.iterator;

import org.apache.wicket.util.lang.*;
import java.util.*;

public abstract class AbstractHierarchyIteratorWithFilter<N,I extends N> extends AbstractHierarchyIterator<N,I>{
    private List<IteratorFilter<N>> filters;
    private List<IteratorFilter<N>> traverseFilter;
    public AbstractHierarchyIteratorWithFilter(final N root){
        super(root);
    }
    protected final boolean onFilter(final N node){
        if(this.filters!=null){
            for(final IteratorFilter<N> filter : this.filters){
                if(!filter.onFilter(node)){
                    return false;
                }
            }
        }
        return true;
    }
    public final List<IteratorFilter<N>> getFilters(){
        if(this.filters==null){
            this.filters=(List<IteratorFilter<N>>)Generics.newArrayList();
        }
        return this.filters;
    }
    public AbstractHierarchyIteratorWithFilter<N,I> addFilter(final IteratorFilter<N> filter){
        Args.notNull((Object)filter,"filter");
        this.getFilters().add(filter);
        return this;
    }
    public Collection<IteratorFilter<N>> replaceFilterSet(final Collection<IteratorFilter<N>> filters){
        final List<IteratorFilter<N>> old=this.filters;
        this.filters=null;
        if(filters!=null&&!filters.isEmpty()){
            for(final IteratorFilter<N> filter : filters){
                this.addFilter(filter);
            }
        }
        return (Collection<IteratorFilter<N>>)old;
    }
    public final I getFirst(final boolean throwException){
        if(this.hasNext()){
            return this.next();
        }
        if(throwException){
            throw new IllegalStateException("Iterator did not match any component");
        }
        return null;
    }
    public final List<I> toList(){
        final List<I> list=(List<I>)Generics.newArrayList();
        for(final I component : this){
            list.add(component);
        }
        return list;
    }
    public final List<IteratorFilter<N>> getTraverseFilters(){
        if(this.traverseFilter==null){
            this.traverseFilter=(List<IteratorFilter<N>>)Generics.newArrayList();
        }
        return this.traverseFilter;
    }
    public AbstractHierarchyIteratorWithFilter<N,I> addTraverseFilters(final IteratorFilter<N> filter){
        this.getTraverseFilters().add(filter);
        return this;
    }
    protected boolean onTraversalFilter(final N node){
        if(this.traverseFilter!=null){
            for(final IteratorFilter<N> filter : this.traverseFilter){
                if(!filter.onFilter(node)){
                    return false;
                }
            }
        }
        return true;
    }
}
