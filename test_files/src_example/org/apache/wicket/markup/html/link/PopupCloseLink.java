package org.apache.wicket.markup.html.link;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.html.*;

public class PopupCloseLink<T> extends Link<T>{
    private static final long serialVersionUID=1L;
    public PopupCloseLink(final String id){
        super(id);
    }
    public PopupCloseLink(final String id,final IModel<T> object){
        super(id,object);
    }
    public void onClick(){
        this.setResponsePage((Class<ClosePopupPage>)ClosePopupPage.class);
    }
    public static final class ClosePopupPage extends WebPage{
        private static final long serialVersionUID=1L;
    }
}
