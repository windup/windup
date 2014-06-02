package org.apache.wicket.markup.repeater;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.model.*;
import java.util.*;
import org.apache.wicket.*;
import java.util.regex.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public abstract class AbstractRepeater extends WebMarkupContainer{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private static Pattern SAFE_CHILD_ID_PATTERN;
    public AbstractRepeater(final String id){
        super(id);
    }
    public AbstractRepeater(final String id,final IModel<?> model){
        super(id,model);
    }
    protected abstract Iterator<? extends Component> renderIterator();
    protected final void onRender(){
        final Iterator<? extends Component> it=this.renderIterator();
        while(it.hasNext()){
            final Component child=(Component)it.next();
            if(child==null){
                throw new IllegalStateException("The render iterator returned null for a child. Container: "+this.toString()+"; Iterator="+it.toString());
            }
            this.renderChild(child);
        }
    }
    protected void renderChild(final Component child){
        child.render();
    }
    protected void onBeforeRender(){
        this.onPopulate();
        if(this.getApplication().usesDevelopmentConfig()){
            for(final Component c : this){
                final Matcher matcher=AbstractRepeater.SAFE_CHILD_ID_PATTERN.matcher((CharSequence)c.getId());
                if(!matcher.matches()){
                    AbstractRepeater.log.warn("Child component of repeater "+this.getClass().getName()+":"+this.getId()+" has a non-safe child id of "+c.getId()+". Safe child ids must be composed of digits only.");
                    break;
                }
            }
        }
        super.onBeforeRender();
    }
    public IMarkupFragment getMarkup(final Component child){
        return this.getMarkup();
    }
    protected abstract void onPopulate();
    static{
        log=LoggerFactory.getLogger(AbstractRepeater.class);
        AbstractRepeater.SAFE_CHILD_ID_PATTERN=Pattern.compile("^\\d+$");
    }
}
