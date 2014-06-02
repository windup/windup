package org.apache.wicket.markup.repeater;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;

public class OddEvenItem<T> extends Item<T>{
    private static final long serialVersionUID=1L;
    private static final String CLASS_EVEN="even";
    private static final String CLASS_ODD="odd";
    public OddEvenItem(final String id,final int index,final IModel<T> model){
        super(id,index,model);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.put("class",(CharSequence)((this.getIndex()%2==0)?"even":"odd"));
    }
}
