package org.apache.wicket.util.iterator;

import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.*;

public class GenericComponentHierarchyIterator<I extends Component> extends AbstractHierarchyIteratorWithFilter<Component,I>{
    public GenericComponentHierarchyIterator(final Component component,final Class<? extends I> clazz){
        super(component);
        Args.notNull((Object)clazz,"clazz");
        this.filterByClass(clazz);
    }
    protected Iterator<Component> newIterator(final Component node){
        return ((MarkupContainer)node).iterator();
    }
    protected boolean hasChildren(final Component elem){
        return elem instanceof MarkupContainer&&((MarkupContainer)elem).size()>0;
    }
    public final GenericComponentHierarchyIterator<I> filterLeavesOnly(){
        this.getFilters().add(new IteratorFilter<Component>(){
            protected boolean onFilter(final Component component){
                return !(component instanceof MarkupContainer)||((MarkupContainer)component).size()==0;
            }
        });
        return this;
    }
    public GenericComponentHierarchyIterator<I> filterByClass(final Class<?> clazz){
        if(clazz!=null){
            this.getFilters().add(new IteratorFilter<Component>(){
                protected boolean onFilter(final Component component){
                    return clazz.isInstance(component);
                }
            });
        }
        return this;
    }
    public GenericComponentHierarchyIterator<I> filterByVisibility(){
        final IteratorFilter<Component> filter=new IteratorFilter<Component>(){
            protected boolean onFilter(final Component comp){
                return comp.isVisibleInHierarchy();
            }
        };
        this.addFilter(filter);
        this.addTraverseFilters(filter);
        return this;
    }
    public GenericComponentHierarchyIterator<I> filterEnabled(){
        final IteratorFilter<Component> filter=new IteratorFilter<Component>(){
            protected boolean onFilter(final Component comp){
                return comp.isEnabledInHierarchy();
            }
        };
        this.addFilter(filter);
        this.addTraverseFilters(filter);
        return this;
    }
    public GenericComponentHierarchyIterator<I> filterById(final String match){
        Args.notEmpty((CharSequence)match,"match");
        this.getFilters().add(new IteratorFilter<Component>(){
            protected boolean onFilter(final Component comp){
                return comp.getId().matches(match);
            }
        });
        return this;
    }
    public GenericComponentHierarchyIterator<I> addFilter(final IteratorFilter<Component> filter){
        super.addFilter(filter);
        return this;
    }
    public GenericComponentHierarchyIterator<I> addTraverseFilters(final IteratorFilter<Component> filter){
        super.addTraverseFilters(filter);
        return this;
    }
}
