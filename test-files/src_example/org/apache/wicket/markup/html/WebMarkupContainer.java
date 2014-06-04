package org.apache.wicket.markup.html;

import org.apache.wicket.*;
import org.apache.wicket.model.*;
import org.apache.wicket.request.http.*;

public class WebMarkupContainer extends MarkupContainer{
    private static final long serialVersionUID=1L;
    public WebMarkupContainer(final String id){
        this(id,null);
    }
    public WebMarkupContainer(final String id,final IModel<?> model){
        super(id,model);
    }
    public final WebPage getWebPage(){
        return (WebPage)this.getPage();
    }
    public final WebRequest getWebRequest(){
        return (WebRequest)this.getRequest();
    }
}
