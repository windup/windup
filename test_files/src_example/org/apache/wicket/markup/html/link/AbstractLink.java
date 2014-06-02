package org.apache.wicket.markup.html.link;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;
import org.apache.wicket.markup.*;

public abstract class AbstractLink extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    private IModel<?> bodyModel;
    private String beforeDisabledLink;
    private String afterDisabledLink;
    public AbstractLink(final String id){
        this(id,null);
    }
    public AbstractLink(final String id,final IModel<?> model){
        super(id,model);
    }
    public AbstractLink setAfterDisabledLink(final String afterDisabledLink){
        if(afterDisabledLink==null){
            throw new IllegalArgumentException("Value cannot be null.  For no text, specify an empty String instead.");
        }
        this.afterDisabledLink=afterDisabledLink;
        return this;
    }
    public String getAfterDisabledLink(){
        return this.afterDisabledLink;
    }
    public AbstractLink setBeforeDisabledLink(final String beforeDisabledLink){
        if(beforeDisabledLink==null){
            throw new IllegalArgumentException("Value cannot be null.  For no text, specify an empty String instead.");
        }
        this.beforeDisabledLink=beforeDisabledLink;
        return this;
    }
    protected void onBeforeRender(){
        super.onBeforeRender();
        if(this.beforeDisabledLink==null){
            final Application app=this.getApplication();
            this.beforeDisabledLink=app.getMarkupSettings().getDefaultBeforeDisabledLink();
            this.afterDisabledLink=app.getMarkupSettings().getDefaultAfterDisabledLink();
        }
    }
    public String getBeforeDisabledLink(){
        return this.beforeDisabledLink;
    }
    protected boolean isLinkEnabled(){
        return this.isEnabledInHierarchy();
    }
    public void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        if(!this.isLinkEnabled()&&this.getBeforeDisabledLink()!=null){
            this.getResponse().write((CharSequence)this.getBeforeDisabledLink());
        }
        final IModel<?> tmpBodyModel=this.getBody();
        if(tmpBodyModel!=null&&tmpBodyModel.getObject()!=null){
            this.replaceComponentTagBody(markupStream,openTag,(CharSequence)this.getDefaultModelObjectAsString(tmpBodyModel.getObject()));
        }
        else{
            super.onComponentTagBody(markupStream,openTag);
        }
        if(!this.isLinkEnabled()&&this.getAfterDisabledLink()!=null){
            this.getResponse().write((CharSequence)this.getAfterDisabledLink());
        }
    }
    protected void disableLink(final ComponentTag tag){
        if(tag.getName().equalsIgnoreCase("a")||tag.getName().equalsIgnoreCase("link")||tag.getName().equalsIgnoreCase("area")){
            tag.setName("span");
            tag.remove("href");
            tag.remove("onclick");
        }
        else if("button".equalsIgnoreCase(tag.getName())||"input".equalsIgnoreCase(tag.getName())){
            tag.put("disabled",(CharSequence)"disabled");
        }
    }
    public IModel<?> getBody(){
        return this.bodyModel;
    }
    public AbstractLink setBody(final IModel<?> bodyModel){
        this.bodyModel=this.wrap(bodyModel);
        return this;
    }
    protected void onDetach(){
        super.onDetach();
        if(this.bodyModel!=null){
            this.bodyModel.detach();
        }
    }
}
