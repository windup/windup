package org.apache.wicket.markup.repeater;

import org.apache.wicket.model.*;
import java.util.*;
import org.apache.wicket.*;

public class RepeatingView extends AbstractRepeater{
    private static final long serialVersionUID=1L;
    private long childIdCounter;
    public RepeatingView(final String id){
        super(id);
        this.childIdCounter=0L;
    }
    public RepeatingView(final String id,final IModel<?> model){
        super(id,model);
        this.childIdCounter=0L;
    }
    public String newChildId(){
        ++this.childIdCounter;
        return String.valueOf(this.childIdCounter);
    }
    protected Iterator<? extends Component> renderIterator(){
        return this.iterator();
    }
    protected void onPopulate(){
    }
}
