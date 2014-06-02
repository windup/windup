package org.apache.wicket.markup.transformer;

import org.apache.wicket.model.*;
import org.apache.wicket.request.*;
import org.apache.wicket.response.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.*;

public abstract class AbstractOutputTransformerContainer extends MarkupContainer implements ITransformer{
    private static final long serialVersionUID=1L;
    private boolean transformBodyOnly;
    public AbstractOutputTransformerContainer(final String id){
        super(id);
        this.transformBodyOnly=true;
    }
    public AbstractOutputTransformerContainer(final String id,final IModel<?> model){
        super(id,model);
        this.transformBodyOnly=true;
    }
    public MarkupContainer setTransformBodyOnly(final boolean value){
        this.transformBodyOnly=value;
        return this;
    }
    protected Response newResponse(){
        return new StringResponse();
    }
    public abstract CharSequence transform(final Component p0,final CharSequence p1) throws Exception;
    public final void onComponentTagBody(final MarkupStream markupStream,final ComponentTag openTag){
        if(this.transformBodyOnly){
            this.execute(new Runnable(){
                public void run(){
                    AbstractOutputTransformerContainer.this.onComponentTagBody(markupStream,openTag);
                }
            });
        }
        else{
            super.onComponentTagBody(markupStream,openTag);
        }
    }
    protected final void onRender(){
        if(!this.transformBodyOnly){
            this.execute(new Runnable(){
                public void run(){
                    AbstractOutputTransformerContainer.this.onRender();
                }
            });
        }
        else{
            super.onRender();
        }
    }
    private final void execute(final Runnable code){
        final Response webResponse=this.getResponse();
        try{
            final Response response=this.newResponse();
            if(response==null){
                throw new IllegalStateException("newResponse() must not return null");
            }
            this.getRequestCycle().setResponse(response);
            code.run();
            try{
                final CharSequence output=this.transform(this,(CharSequence)response.toString());
                webResponse.write(output);
            }
            catch(Exception ex){
                throw new WicketRuntimeException("Error while transforming the output: "+this,ex);
            }
        }
        finally{
            this.getRequestCycle().setResponse(webResponse);
        }
    }
}
