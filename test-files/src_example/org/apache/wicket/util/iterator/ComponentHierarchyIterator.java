package org.apache.wicket.util.iterator;

import java.util.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class ComponentHierarchyIterator extends AbstractHierarchyIteratorWithFilter<Component,Component>{
    public ComponentHierarchyIterator(final Component component){
        super(component);
    }
    public ComponentHierarchyIterator(final Component component,final Class<?> clazz,final boolean visible,final boolean enabled){
        this(component);
        if(clazz!=null){
            this.filterByClass(clazz);
        }
        if(visible){
            this.filterByVisibility();
        }
        if(enabled){
            this.filterEnabled();
        }
    }
    public ComponentHierarchyIterator(final Component component,final Class<?> clazz){
        this(component,clazz,false,false);
    }
    protected Iterator<Component> newIterator(final Component node){
        return ((MarkupContainer)node).iterator();
    }
    protected boolean hasChildren(final Component elem){
        return elem instanceof MarkupContainer&&((MarkupContainer)elem).size()>0;
    }
    public final ComponentHierarchyIterator filterLeavesOnly(){
        this.getFilters().add(new IteratorFilter<Component>(){
            protected boolean onFilter(final Component component){
                return !(component instanceof MarkupContainer)||((MarkupContainer)component).size()==0;
            }
        });
        return this;
    }
    public ComponentHierarchyIterator filterByClass(final Class<?> clazz){
        if(clazz!=null){
            this.getFilters().add(new IteratorFilter<Component>(){
                protected boolean onFilter(final Component component){
                    return clazz.isInstance(component);
                }
            });
        }
        return this;
    }
    public ComponentHierarchyIterator filterByVisibility(){
        final IteratorFilter<Component> filter=new IteratorFilter<Component>(){
            protected boolean onFilter(final Component comp){
                return comp.isVisibleInHierarchy();
            }
        };
        this.addFilter(filter);
        this.addTraverseFilters(filter);
        return this;
    }
    public ComponentHierarchyIterator filterEnabled(){
        final IteratorFilter<Component> filter=new IteratorFilter<Component>(){
            protected boolean onFilter(final Component comp){
                return comp.isEnabledInHierarchy();
            }
        };
        this.addFilter(filter);
        this.addTraverseFilters(filter);
        return this;
    }
    public ComponentHierarchyIterator filterById(final String match){
        Args.notEmpty((CharSequence)match,"match");
        this.getFilters().add(new IteratorFilter<Component>(){
            protected boolean onFilter(final Component comp){
                return comp.getId().matches(match);
            }
        });
        return this;
    }
    public ComponentHierarchyIterator addFilter(final IteratorFilter<Component> filter){
        super.addFilter(filter);
        return this;
    }
    public ComponentHierarchyIterator addTraverseFilters(final IteratorFilter<Component> filter){
        super.addTraverseFilters(filter);
        return this;
    }
}
