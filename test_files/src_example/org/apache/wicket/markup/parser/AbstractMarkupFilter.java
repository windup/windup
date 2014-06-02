package org.apache.wicket.markup.parser;

import java.text.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public abstract class AbstractMarkupFilter implements IMarkupFilter{
    private static final Logger log;
    private final MarkupResourceStream markupResourceStream;
    private IMarkupFilter parent;
    public AbstractMarkupFilter(){
        this(null);
    }
    public AbstractMarkupFilter(final MarkupResourceStream markupResourceStream){
        super();
        this.markupResourceStream=markupResourceStream;
    }
    public IMarkupFilter getNextFilter(){
        return this.parent;
    }
    public void setNextFilter(final IMarkupFilter parent){
        this.parent=parent;
    }
    public MarkupElement nextElement() throws ParseException{
        MarkupElement elem=this.getNextFilter().nextElement();
        if(elem!=null){
            if(elem instanceof ComponentTag){
                elem=this.onComponentTag((ComponentTag)elem);
            }
            else if(elem instanceof HtmlSpecialTag){
                elem=this.onSpecialTag((HtmlSpecialTag)elem);
            }
        }
        return elem;
    }
    protected abstract MarkupElement onComponentTag(final ComponentTag p0) throws ParseException;
    protected MarkupElement onSpecialTag(final HtmlSpecialTag tag) throws ParseException{
        return tag;
    }
    public void postProcess(final Markup markup){
    }
    protected MarkupResourceStream getMarkupResourceStream(){
        return this.markupResourceStream;
    }
    protected String getWicketNamespace(){
        String wicketNamespace;
        if(this.markupResourceStream!=null){
            wicketNamespace=this.markupResourceStream.getWicketNamespace();
        }
        else{
            wicketNamespace="wicket";
        }
        return wicketNamespace;
    }
    static{
        log=LoggerFactory.getLogger(AbstractMarkupFilter.class);
    }
}
