package org.apache.wicket;

import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.html.form.*;

public class Initializer implements IInitializer{
    public void init(final Application application){
        IBehaviorListener.INTERFACE.register();
        IFormSubmitListener.INTERFACE.register();
        ILinkListener.INTERFACE.register();
        IOnChangeListener.INTERFACE.register();
        IRedirectListener.INTERFACE.register();
        IResourceListener.INTERFACE.register();
    }
    public String toString(){
        return "Wicket core library initializer";
    }
    public void destroy(final Application application){
    }
}
