package org.apache.wicket.application;

import org.apache.wicket.util.listener.*;
import org.apache.wicket.markup.html.*;

public class HeaderContributorListenerCollection extends ListenerCollection<IHeaderContributor> implements IHeaderContributor{
    private static final long serialVersionUID=1L;
    public void renderHead(final IHeaderResponse response){
        this.notify((ListenerCollection.INotifier)new ListenerCollection.INotifier<IHeaderContributor>(){
            public void notify(final IHeaderContributor listener){
                listener.renderHead(response);
            }
        });
    }
}
