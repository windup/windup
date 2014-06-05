package org.apache.wicket.ajax.calldecorator;

import org.apache.wicket.ajax.*;
import org.apache.wicket.*;

public abstract class AjaxCallDecorator implements IAjaxCallDecorator{
    private static final long serialVersionUID=1L;
    public CharSequence decorateScript(final Component c,final CharSequence script){
        return script;
    }
    public CharSequence decorateOnSuccessScript(final Component c,final CharSequence script){
        return script;
    }
    public CharSequence decorateOnFailureScript(final Component c,final CharSequence script){
        return script;
    }
}
