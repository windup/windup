package org.apache.wicket.ajax.markup.html;

import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.model.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.ajax.calldecorator.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.ajax.*;
import org.slf4j.*;

public abstract class AjaxFallbackLink<T> extends Link<T> implements IAjaxLink{
    private static final Logger LOG;
    private static final long serialVersionUID=1L;
    public AjaxFallbackLink(final String id){
        this(id,null);
    }
    public AjaxFallbackLink(final String id,final IModel<T> model){
        super(id,model);
    }
    protected void onInitialize(){
        super.onInitialize();
        this.add(this.newAjaxEventBehavior("onclick"));
    }
    protected AjaxEventBehavior newAjaxEventBehavior(final String event){
        return new AjaxEventBehavior(event){
            private static final long serialVersionUID=1L;
            protected void onEvent(final AjaxRequestTarget target){
                AjaxFallbackLink.this.onClick(target);
            }
            protected IAjaxCallDecorator getAjaxCallDecorator(){
                return new CancelEventIfNoAjaxDecorator(AjaxFallbackLink.this.getAjaxCallDecorator());
            }
            protected void onComponentTag(final ComponentTag tag){
                if(AjaxFallbackLink.this.isLinkEnabled()){
                    super.onComponentTag(tag);
                }
            }
            protected AjaxChannel getChannel(){
                return AjaxFallbackLink.this.getChannel();
            }
        };
    }
    protected AjaxChannel getChannel(){
        return null;
    }
    protected IAjaxCallDecorator getAjaxCallDecorator(){
        return null;
    }
    public final void onClick(){
        this.onClick(null);
    }
    public abstract void onClick(final AjaxRequestTarget p0);
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        final String tagName=tag.getName();
        if(this.isLinkEnabled()&&AjaxFallbackLink.LOG.isWarnEnabled()&&!"a".equalsIgnoreCase(tagName)&&!"area".equalsIgnoreCase(tagName)&&!"link".equalsIgnoreCase(tagName)){
            AjaxFallbackLink.LOG.warn("{} must be used only with <a>, <area> or <link> markup elements. The fallback functionality doesn't work for other markup elements. Component path: {}, markup element: <{}>.",AjaxFallbackLink.class.getSimpleName(),this.getClassRelativePath(),tagName);
        }
    }
    static{
        LOG=LoggerFactory.getLogger(AjaxFallbackLink.class);
    }
}
