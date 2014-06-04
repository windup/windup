package org.apache.wicket.markup.transformer;

import org.apache.wicket.markup.*;
import org.apache.wicket.*;

public class XsltTransformerBehavior extends AbstractTransformerBehavior{
    private static final long serialVersionUID=1L;
    private final String xslFile;
    public XsltTransformerBehavior(){
        super();
        this.xslFile=null;
    }
    public XsltTransformerBehavior(final String xslFilePath){
        super();
        this.xslFile=xslFilePath;
    }
    public void onComponentTag(final Component component,final ComponentTag tag){
        tag.put("xmlns:wicket",(CharSequence)"http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd");
        super.onComponentTag(component,tag);
    }
    public CharSequence transform(final Component component,final CharSequence output) throws Exception{
        return new XsltTransformer(this.xslFile).transform(component,output);
    }
    public void bind(final Component component){
        if(component instanceof Page){
            throw new WicketRuntimeException("You can not attach a XstlTransformerBehavior to a Page. It can be attached to any other component.");
        }
        super.bind(component);
    }
}
