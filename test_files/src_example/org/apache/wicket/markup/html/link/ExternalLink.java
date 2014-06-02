package org.apache.wicket.markup.html.link;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.util.string.*;

public class ExternalLink extends AbstractLink{
    private static final long serialVersionUID=1L;
    private boolean contextRelative;
    private PopupSettings popupSettings;
    public ExternalLink(final String id,final String href,final String label){
        super(id);
        this.contextRelative=false;
        this.popupSettings=null;
        this.setDefaultModel((href!=null)?new Model<Object>(href):null);
        this.setBody(Model.of(label));
    }
    public ExternalLink(final String id,final String href){
        this(id,Model.of(href));
    }
    public ExternalLink(final String id,final IModel<String> href){
        this(id,href,null);
    }
    public ExternalLink(final String id,final IModel<String> href,final IModel<?> label){
        super(id);
        this.contextRelative=false;
        this.popupSettings=null;
        this.setDefaultModel(this.wrap(href));
        this.setBody(label);
    }
    public final PopupSettings getPopupSettings(){
        return this.popupSettings;
    }
    public final ExternalLink setPopupSettings(final PopupSettings popupSettings){
        this.popupSettings=popupSettings;
        return this;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(!this.isLinkEnabled()){
            this.disableLink(tag);
        }
        else if(this.getDefaultModel()!=null){
            final Object hrefValue=this.getDefaultModelObject();
            if(hrefValue!=null){
                String url=hrefValue.toString();
                if(this.contextRelative){
                    if(url.length()>0&&url.charAt(0)=='/'){
                        url=url.substring(1);
                    }
                    url=UrlUtils.rewriteToContextRelative(url,RequestCycle.get());
                }
                if(tag.getName().equalsIgnoreCase("a")||tag.getName().equalsIgnoreCase("link")||tag.getName().equalsIgnoreCase("area")){
                    tag.put("href",(CharSequence)url);
                    if(this.popupSettings!=null){
                        tag.put("onclick",(CharSequence)this.popupSettings.getPopupJavaScript());
                    }
                }
                else if(this.popupSettings!=null){
                    this.popupSettings.setTarget("'"+url+"'");
                    final String popupScript=this.popupSettings.getPopupJavaScript();
                    tag.put("onclick",(CharSequence)popupScript);
                }
                else{
                    tag.put("onclick",(CharSequence)("window.location.href='"+url+"';return false;"));
                }
            }
        }
    }
    public boolean isContextRelative(){
        return this.contextRelative;
    }
    public ExternalLink setContextRelative(final boolean contextRelative){
        this.contextRelative=contextRelative;
        return this;
    }
    @Deprecated
    public IModel<?> getLabel(){
        return this.getBody();
    }
}
