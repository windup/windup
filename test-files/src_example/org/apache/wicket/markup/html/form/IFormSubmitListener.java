package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;

public interface IFormSubmitListener extends IRequestListener{
    public static final RequestListenerInterface INTERFACE=new RequestListenerInterface((Class<? extends IRequestListener>)IFormSubmitListener.class);
    void onFormSubmitted();
}
