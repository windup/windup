package org.apache.wicket.ajax;

import org.apache.wicket.util.time.*;
import org.apache.wicket.*;

public class AjaxSelfUpdatingTimerBehavior extends AbstractAjaxTimerBehavior{
    private static final long serialVersionUID=1L;
    public AjaxSelfUpdatingTimerBehavior(final Duration updateInterval){
        super(updateInterval);
    }
    protected final void onTimer(final AjaxRequestTarget target){
        target.add(this.getComponent());
        this.onPostProcessTarget(target);
    }
    protected void onPostProcessTarget(final AjaxRequestTarget target){
    }
}
