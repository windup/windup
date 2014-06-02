package org.apache.wicket.markup.html;

import org.apache.wicket.*;
import org.apache.wicket.model.*;

public class WebComponent extends Component{
    private static final long serialVersionUID=1L;
    public WebComponent(final String id){
        super(id);
    }
    public WebComponent(final String id,final IModel<?> model){
        super(id,model);
    }
    protected void onRender(){
        this.internalRenderComponent();
    }
}
