package org.apache.wicket.markup.html.form;

import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;

public class SubmitLink extends AbstractSubmitLink{
    private static final long serialVersionUID=1L;
    public SubmitLink(final String id){
        super(id);
    }
    public SubmitLink(final String id,final Form<?> form){
        super(id,form);
    }
    public SubmitLink(final String id,final IModel<?> model){
        super(id,model);
    }
    public SubmitLink(final String id,final IModel<?> model,final Form<?> form){
        super(id,model,form);
    }
    public final void onLinkClicked(){
        this.onSubmit();
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        if(!this.isLinkEnabled()){
            this.disableLink(tag);
        }
        else{
            if(tag.getName().equalsIgnoreCase("a")){
                tag.put("href",(CharSequence)"#");
            }
            tag.put("onclick",(CharSequence)this.getTriggerJavaScript());
        }
    }
    protected boolean shouldInvokeJavaScriptFormOnsubmit(){
        return true;
    }
    protected String getTriggerJavaScript(){
        if(this.getForm()!=null){
            final Form<?> root=this.getForm().getRootForm();
            final StringBuilder sb=new StringBuilder(100);
            sb.append("var e=document.getElementById('");
            sb.append(root.getHiddenFieldId());
            sb.append("'); e.name='");
            sb.append(this.getInputName());
            sb.append("'; e.value='x';");
            sb.append("var f=document.getElementById('");
            sb.append(root.getMarkupId());
            sb.append("');");
            if(this.shouldInvokeJavaScriptFormOnsubmit()){
                if(this.getForm()!=root){
                    sb.append("var ff=document.getElementById('");
                    sb.append(this.getForm().getMarkupId());
                    sb.append("');");
                }
                else{
                    sb.append("var ff=f;");
                }
                sb.append("if (ff.onsubmit != undefined) { if (ff.onsubmit()==false) return false; }");
            }
            sb.append("f.submit();e.value='';e.name='';return false;");
            return sb.toString();
        }
        return null;
    }
    public void onSubmit(){
    }
    public void onError(){
    }
    public void onAfterSubmit(){
    }
}
