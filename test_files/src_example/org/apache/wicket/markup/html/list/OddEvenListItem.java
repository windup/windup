package org.apache.wicket.markup.html.list;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;

public class OddEvenListItem<T> extends ListItem<T>{
    private static final long serialVersionUID=1L;
    private static final String CLASS_EVEN="even";
    private static final String CLASS_ODD="odd";
    public OddEvenListItem(final int index,final IModel<T> model){
        super(index,model);
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        tag.append("class",(CharSequence)((this.getIndex()%2==0)?"even":"odd")," ");
    }
}
