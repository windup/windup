package org.apache.wicket.markup.html.form;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.request.resource.*;

public abstract class AbstractCheckSelector extends LabeledWebMarkupContainer implements IHeaderContributor{
    private static final long serialVersionUID=1L;
    private static final ResourceReference JS;
    public AbstractCheckSelector(final String id){
        super(id);
        this.setOutputMarkupId(true);
    }
    protected boolean wantAutomaticUpdate(){
        return true;
    }
    public void renderHead(final IHeaderResponse response){
        response.renderJavaScriptReference(WicketEventReference.INSTANCE);
        response.renderJavaScriptReference(AbstractCheckSelector.JS);
        final String findCheckboxes=this.getFindCheckboxesFunction().toString();
        response.renderOnLoadJavaScript("Wicket.CheckboxSelector.initializeSelector('"+this.getMarkupId()+"', "+findCheckboxes+");");
        if(this.wantAutomaticUpdate()){
            response.renderOnLoadJavaScript("Wicket.CheckboxSelector.attachUpdateHandlers('"+this.getMarkupId()+"', "+findCheckboxes+");");
        }
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(this.isEnableAllowed()&&this.isEnabledInHierarchy()){
            tag.remove("disabled");
        }
        else{
            tag.put("disabled",(CharSequence)"disabled");
        }
        this.checkComponentTag(tag,"input");
        this.checkComponentTagAttribute(tag,"type","checkbox");
    }
    protected abstract CharSequence getFindCheckboxesFunction();
    static{
        JS=new PackageResourceReference((Class<?>)AbstractCheckSelector.class,"CheckSelector.js");
    }
}
