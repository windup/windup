package org.apache.wicket.request.mapper;

import org.apache.wicket.*;
import org.apache.wicket.request.component.*;

public class StalePageException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    private final transient IRequestablePage page;
    public StalePageException(final IRequestablePage page){
        super();
        this.page=page;
    }
    public IRequestablePage getPage(){
        return this.page;
    }
}
