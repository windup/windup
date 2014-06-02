package org.apache.wicket.markup.html.form;

import org.apache.wicket.*;

public interface IOnChangeListener extends IRequestListener{
    public static final RequestListenerInterface INTERFACE=new RequestListenerInterface((Class<? extends IRequestListener>)IOnChangeListener.class);
    void onSelectionChanged();
}
