package org.apache.wicket.markup.html.basic;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class EnclosureContainer extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    private final Component child;
    public EnclosureContainer(final String id,final Component child){
        super(id);
        Args.notNull((Object)child,"child");
        this.child=child;
        this.setRenderBodyOnly(true);
    }
    public boolean isVisible(){
        this.child.configure();
        return this.child.determineVisibility();
    }
}
