package org.apache.wicket.markup.html.link;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.*;

public abstract class Link<T> extends AbstractLink implements ILinkListener{
    private static final long serialVersionUID=1L;
    private Component anchor;
    private boolean autoEnable;
    private PopupSettings popupSettings;
    public Link(final String id){
        super(id);
        this.autoEnable=false;
        this.popupSettings=null;
    }
    public Link(final String id,final IModel<T> model){
        super(id,model);
        this.autoEnable=false;
        this.popupSettings=null;
    }
    public Component getAnchor(){
        return this.anchor;
    }
    public final boolean getAutoEnable(){
        return this.autoEnable;
    }
    public final PopupSettings getPopupSettings(){
        return this.popupSettings;
    }
    public boolean isEnabled(){
        if(this.getAutoEnable()){
            return !this.linksTo(this.getPage());
        }
        return super.isEnabled();
    }
    protected boolean getStatelessHint(){
        return false;
    }
    public abstract void onClick();
    public final void onLinkClicked(){
        this.onClick();
    }
    public Link<T> setAnchor(final Component anchor){
        this.addStateChange();
        this.anchor=anchor;
        return this;
    }
    public final Link<T> setAutoEnable(final boolean autoEnable){
        this.autoEnable=autoEnable;
        return this;
    }
    public final Link<T> setPopupSettings(final PopupSettings popupSettings){
        this.popupSettings=popupSettings;
        return this;
    }
    protected CharSequence appendAnchor(final ComponentTag tag,CharSequence url){
        if(url!=null){
            final Component anchor=this.getAnchor();
            if(anchor!=null){
                if(url.toString().indexOf(35)==-1){
                    String id;
                    if(anchor.getOutputMarkupId()){
                        id=anchor.getMarkupId();
                    }
                    else{
                        id=anchor.getMarkupAttributes().getString("id");
                    }
                    if(id==null){
                        throw new WicketRuntimeException("an achor component was set on "+this+" but it neither has outputMarkupId set to true "+"nor has a id set explicitly");
                    }
                    url=(CharSequence)((Object)url+"#"+anchor.getMarkupId());
                }
            }
            else if(tag.getName().equalsIgnoreCase("a")&&url.toString().indexOf(35)==-1){
                final String href=tag.getAttributes().getString("href");
                if(href!=null&&href.length()>1&&href.charAt(0)=='#'){
                    url=(CharSequence)((Object)url+href);
                }
            }
        }
        return url;
    }
    protected CharSequence getOnClickScript(final CharSequence url){
        return null;
    }
    protected CharSequence getURL(){
        return this.urlFor(ILinkListener.INTERFACE,new PageParameters());
    }
    protected boolean linksTo(final Page page){
        return false;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(!this.isLinkEnabled()){
            this.disableLink(tag);
        }
        else{
            CharSequence url=this.getURL();
            url=this.appendAnchor(tag,url);
            if(tag.getName().equalsIgnoreCase("a")||tag.getName().equalsIgnoreCase("link")||tag.getName().equalsIgnoreCase("area")){
                tag.put("href",url);
                if(this.popupSettings!=null){
                    tag.put("onclick",(CharSequence)this.popupSettings.getPopupJavaScript());
                }
            }
            else if(tag.getName().equalsIgnoreCase("script")||tag.getName().equalsIgnoreCase("style")){
                tag.put("src",url);
            }
            else if(this.popupSettings!=null){
                this.popupSettings.setTarget("'"+(Object)url+"'");
                final String popupScript=this.popupSettings.getPopupJavaScript();
                tag.put("onclick",(CharSequence)popupScript);
            }
            else{
                tag.put("onclick",(CharSequence)("var win = this.ownerDocument.defaultView || this.ownerDocument.parentWindow; if (win == window) { window.location.href='"+(Object)url+"'; } ;return false"));
            }
            final CharSequence onClickJavaScript=this.getOnClickScript(url);
            if(onClickJavaScript!=null){
                tag.put("onclick",onClickJavaScript);
            }
        }
    }
    public final IModel<T> getModel(){
        return (IModel<T>)this.getDefaultModel();
    }
    public final void setModel(final IModel<T> model){
        this.setDefaultModel(model);
    }
    public final T getModelObject(){
        return (T)this.getDefaultModelObject();
    }
    public final void setModelObject(final T object){
        this.setDefaultModelObject(object);
    }
}
