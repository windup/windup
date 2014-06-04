package org.apache.wicket.ajax;

import org.apache.wicket.util.string.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;
import org.apache.wicket.*;

public abstract class AjaxEventBehavior extends AbstractDefaultAjaxBehavior{
    private static final Logger LOG;
    private static long sequence;
    private static final long serialVersionUID=1L;
    private final String event;
    private ThrottlingSettings throttlingSettings;
    public AjaxEventBehavior(final String event){
        super();
        if(Strings.isEmpty((CharSequence)event)){
            throw new IllegalArgumentException("argument [event] cannot be null or empty");
        }
        this.onCheckEvent(event);
        this.event=event;
    }
    public final AjaxEventBehavior setThrottleDelay(final Duration throttleDelay){
        this.throttlingSettings=new ThrottlingSettings("th"+++AjaxEventBehavior.sequence,throttleDelay);
        return this;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        final Component myComponent=this.getComponent();
        if(myComponent.isEnabledInHierarchy()){
            if(AjaxEventBehavior.LOG.isWarnEnabled()&&myComponent.getApplication().usesDevelopmentConfig()){
                final String attribute=tag.getAttribute(this.event);
                if(!Strings.isEmpty((CharSequence)attribute)){
                    AjaxEventBehavior.LOG.warn("{} assigned to {} is overriding the previous value of the inline attribute. Maybe there are several Ajax event behaviors on the same type assigned to this component.",new Object[] { this,myComponent });
                }
            }
            tag.put(this.event,this.getEventHandler());
        }
    }
    protected CharSequence getEventHandler(){
        CharSequence handler=this.getCallbackScript();
        if(this.event.equalsIgnoreCase("href")){
            handler=(CharSequence)("javascript:"+(Object)handler);
        }
        return handler;
    }
    protected CharSequence generateCallbackScript(final CharSequence partialCall){
        CharSequence script=super.generateCallbackScript(partialCall);
        final ThrottlingSettings ts=this.throttlingSettings;
        if(ts!=null){
            script=AbstractDefaultAjaxBehavior.throttleScript(script,ts.getId(),ts.getDelay());
        }
        return script;
    }
    protected void onCheckEvent(final String event){
    }
    public final String getEvent(){
        return this.event;
    }
    protected final void respond(final AjaxRequestTarget target){
        this.onEvent(target);
    }
    protected abstract void onEvent(final AjaxRequestTarget p0);
    public String toString(){
        return this.getClass().getName()+" {"+"event='"+this.event+'\''+'}';
    }
    static{
        LOG=LoggerFactory.getLogger(AjaxEventBehavior.class);
        AjaxEventBehavior.sequence=0L;
    }
    private static class ThrottlingSettings implements IClusterable{
        private static final long serialVersionUID=1L;
        private final Duration delay;
        private final String id;
        public ThrottlingSettings(final String id,final Duration delay){
            super();
            this.id=id;
            this.delay=delay;
        }
        public Duration getDelay(){
            return this.delay;
        }
        public String getId(){
            return this.id;
        }
    }
}
