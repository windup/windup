package org.apache.wicket.ajax.calldecorator;

import org.apache.wicket.ajax.*;
import org.apache.wicket.*;

public final class CancelEventIfNoAjaxDecorator extends AjaxPostprocessingCallDecorator{
    private static final long serialVersionUID=1L;
    public CancelEventIfNoAjaxDecorator(){
        this(null);
    }
    public CancelEventIfNoAjaxDecorator(final IAjaxCallDecorator delegate){
        super(delegate);
    }
    public final CharSequence postDecorateScript(final Component c,final CharSequence script){
        return (CharSequence)((Object)script+"return !"+"wcall"+";");
    }
}
