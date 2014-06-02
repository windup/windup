package org.apache.wicket.markup.transformer;

import org.apache.wicket.model.*;
import org.apache.wicket.*;

public class NoopOutputTransformerContainer extends AbstractOutputTransformerContainer{
    private static final long serialVersionUID=1L;
    public NoopOutputTransformerContainer(final String id){
        super(id);
    }
    public NoopOutputTransformerContainer(final String id,final IModel<?> model){
        super(id,model);
    }
    public CharSequence transform(final Component component,final CharSequence output){
        return output;
    }
}
