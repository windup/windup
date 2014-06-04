package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.request.mapper.parameter.*;

public abstract class AbstractErrorPage extends WebPage{
    private static final long serialVersionUID=1L;
    protected AbstractErrorPage(){
        super();
    }
    protected AbstractErrorPage(final IModel<?> model){
        super(model);
    }
    protected AbstractErrorPage(final PageParameters parameters){
        super(parameters);
    }
    public boolean isErrorPage(){
        return true;
    }
    public boolean isVersioned(){
        return false;
    }
}
