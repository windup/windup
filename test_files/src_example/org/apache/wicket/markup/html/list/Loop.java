package org.apache.wicket.markup.html.list;

import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;
import java.util.*;
import org.apache.wicket.util.collections.*;

public abstract class Loop extends AbstractRepeater{
    private static final long serialVersionUID=1L;
    public Loop(final String id,final int iterations){
        super(id,new Model<Object>((Object)iterations));
    }
    public Loop(final String id,final IModel<Integer> model){
        super(id,model);
    }
    public final int getIterations(){
        return (int)this.getDefaultModelObject();
    }
    protected final void onPopulate(){
        this.removeAll();
        final int iterations=this.getIterations();
        if(iterations>0){
            for(int iteration=0;iteration<iterations;++iteration){
                final LoopItem item=this.newItem(iteration);
                this.add(item);
                this.populateItem(item);
            }
        }
    }
    protected LoopItem newItem(final int iteration){
        return new LoopItem(iteration);
    }
    protected Iterator<Component> renderIterator(){
        final int iterations=this.size();
        return (Iterator<Component>)new ReadOnlyIterator<Component>(){
            private int index=0;
            public boolean hasNext(){
                return this.index<iterations;
            }
            public Component next(){
                return Loop.this.get(Integer.toString(this.index++));
            }
        };
    }
    protected abstract void populateItem(final LoopItem p0);
    protected final void renderChild(final Component child){
        this.renderItem((LoopItem)child);
    }
    protected void renderItem(final LoopItem item){
        item.render();
    }
}
