package org.apache.wicket.markup.html.link;

import org.apache.wicket.*;

public interface ILinkListener extends IRequestListener{
    public static final RequestListenerInterface INTERFACE=new RequestListenerInterface((Class<? extends IRequestListener>)ILinkListener.class);
    void onLinkClicked();
}
