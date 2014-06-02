package org.apache.wicket.markup.transformer;

import org.apache.wicket.behavior.*;
import org.apache.wicket.model.*;
import org.apache.wicket.*;

public class XsltOutputTransformerContainer extends AbstractOutputTransformerContainer{
    private static final long serialVersionUID=1L;
    private final String xslFile;
    public XsltOutputTransformerContainer(final String id,final IModel<?> model,final String xslFilePath){
        super(id);
        this.xslFile=xslFilePath;
        this.setTransformBodyOnly(false);
        this.add(AttributeModifier.replace("xmlns:wicket",Model.of("http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd")));
    }
    public XsltOutputTransformerContainer(final String id,final IModel<?> model){
        this(id,model,null);
    }
    public XsltOutputTransformerContainer(final String id){
        this(id,null,null);
    }
    public CharSequence transform(final Component component,final CharSequence output) throws Exception{
        return new XsltTransformer(this.xslFile).transform(component,output);
    }
}
