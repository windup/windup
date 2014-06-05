package org.apache.wicket.ajax.calldecorator;

import org.apache.wicket.util.time.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;
import org.apache.wicket.ajax.*;

public final class AjaxCallThrottlingDecorator extends AjaxPostprocessingCallDecorator{
    private static final long serialVersionUID=1L;
    private final Duration duration;
    private final String id;
    public AjaxCallThrottlingDecorator(final String id,final Duration delay){
        this(null,id,delay);
    }
    public AjaxCallThrottlingDecorator(final IAjaxCallDecorator decorator,final String id,final Duration delay){
        super(decorator);
        if(Strings.isEmpty((CharSequence)id)){
            throw new IllegalArgumentException("id cannot be an empty string");
        }
        this.id=id;
        this.duration=delay;
    }
    public final CharSequence postDecorateScript(final Component c,final CharSequence script){
        return AbstractDefaultAjaxBehavior.throttleScript(script,this.id,this.duration);
    }
}
