package org.apache.wicket.markup;

import java.util.*;

public class MarkupIterator implements Iterator<MarkupElement>{
    private final IMarkupFragment markup;
    private int index;
    private boolean componentTagOnly;
    private boolean wicketTagOnly;
    private boolean openTagOnly;
    public MarkupIterator(final IMarkupFragment markup){
        super();
        this.index=-1;
        if(markup==null){
            throw new NullPointerException("Parameter 'markup' must not be null");
        }
        this.markup=markup;
    }
    public boolean hasNext(){
        ++this.index;
        while(this.index<this.markup.size()){
            final MarkupElement elem=this.markup.get(this.index);
            if((!this.componentTagOnly||!(elem instanceof ComponentTag))&&(!this.wicketTagOnly||!(elem instanceof WicketTag))){
                return true;
            }
            if(!this.openTagOnly){
                return true;
            }
            final ComponentTag tag=(ComponentTag)elem;
            if(tag.isOpen()){
                return true;
            }
            ++this.index;
        }
        return false;
    }
    public MarkupElement next(){
        return this.markup.get(this.index);
    }
    public ComponentTag nextTag(){
        return (ComponentTag)this.next();
    }
    public WicketTag nextWicketTag(){
        return (WicketTag)this.next();
    }
    public void remove(){
        throw new UnsupportedOperationException("You can not remove markup elements");
    }
    public final void setComponentTagOnly(final boolean componentTagOnly){
        this.componentTagOnly=componentTagOnly;
    }
    public final void setWicketTagOnly(final boolean wicketTagOnly){
        this.wicketTagOnly=wicketTagOnly;
    }
    public final void setOpenTagOnly(final boolean openTagOnly){
        this.openTagOnly=openTagOnly;
    }
}
