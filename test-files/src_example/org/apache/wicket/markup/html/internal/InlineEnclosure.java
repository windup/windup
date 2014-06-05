package org.apache.wicket.markup.html.internal;

import org.apache.wicket.markup.*;
import org.slf4j.*;

public class InlineEnclosure extends Enclosure{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private String enclosureMarkupAsString;
    public InlineEnclosure(final String id,final String childId){
        super(id,(CharSequence)childId);
        this.enclosureMarkupAsString=null;
        this.setOutputMarkupPlaceholderTag(true);
        this.setMarkupId(this.getId());
    }
    protected void onComponentTag(final ComponentTag tag){
        tag.remove("wicket:enclosure");
        super.onComponentTag(tag);
    }
    public boolean updateVisibility(){
        final boolean visible=this.getChild().determineVisibility();
        this.setVisible(visible);
        return visible;
    }
    public IMarkupFragment getMarkup(){
        IMarkupFragment enclosureMarkup=null;
        if(this.enclosureMarkupAsString==null){
            final IMarkupFragment markup=super.getMarkup();
            if(markup!=null&&markup!=Markup.NO_MARKUP){
                enclosureMarkup=markup;
                this.enclosureMarkupAsString=markup.toString(true);
            }
        }
        else{
            enclosureMarkup=Markup.of(this.enclosureMarkupAsString);
        }
        return enclosureMarkup;
    }
    static{
        log=LoggerFactory.getLogger(InlineEnclosure.class);
    }
}
