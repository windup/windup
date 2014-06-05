package org.apache.wicket.behavior;

import org.apache.wicket.*;

@Deprecated
public abstract class AbstractBehavior extends Behavior{
    private static final long serialVersionUID=1L;
    public void cleanup(){
    }
    public final void onException(final Component component,final RuntimeException exception){
        try{
            this.onHandleException(component,exception);
        }
        finally{
            this.cleanup();
        }
    }
    public void onHandleException(final Component component,final RuntimeException exception){
    }
    public void onRendered(final Component component){
    }
    public final void afterRender(final Component component){
        try{
            this.onRendered(component);
        }
        finally{
            this.cleanup();
        }
    }
}
