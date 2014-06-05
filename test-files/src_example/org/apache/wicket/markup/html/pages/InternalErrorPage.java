package org.apache.wicket.markup.html.pages;

import org.apache.wicket.*;
import org.apache.wicket.request.http.*;

public class InternalErrorPage extends AbstractErrorPage{
    private static final long serialVersionUID=1L;
    public InternalErrorPage(){
        super();
        this.add(this.homePageLink("homePageLink"));
    }
    protected void setHeaders(final WebResponse response){
        response.setStatus(500);
    }
}
