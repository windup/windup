package org.apache.wicket.markup.html.list;

import org.apache.wicket.model.*;

public class LoopItem extends AbstractItem{
    private static final long serialVersionUID=1L;
    private int index;
    public LoopItem(final int index){
        super(index);
        this.index=index;
    }
    public LoopItem(final int index,final IModel<?> model){
        super(index,model);
        this.index=index;
    }
    public LoopItem(final String id,final int index,final IModel<?> model){
        super(id,model);
        this.index=index;
    }
    public LoopItem(final String id,final int index){
        super(id);
        this.index=index;
    }
    public final int getIndex(){
        return this.index;
    }
    public final void setIndex(final int index){
        if(this.index!=index){
            if(this.isVersioned()){
                this.addStateChange();
            }
            this.index=index;
        }
    }
}
