package org.apache.wicket.markup.html.list;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;

public class AbstractItem extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    public AbstractItem(final String id,final IModel<?> model){
        super(id.intern(),model);
    }
    public AbstractItem(final String id){
        super(id.intern());
    }
    public AbstractItem(final int id,final IModel<?> model){
        this(Integer.toString(id),model);
    }
    public AbstractItem(final int id){
        this(Integer.toString(id));
    }
}
