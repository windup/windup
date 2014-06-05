package org.apache.wicket.markup.parser.filter;

import java.text.*;
import org.apache.wicket.markup.parser.*;
import org.apache.wicket.markup.*;

public final class RootMarkupFilter extends AbstractMarkupFilter{
    private final IXmlPullParser parser;
    public RootMarkupFilter(final IXmlPullParser parser){
        super();
        this.parser=parser;
    }
    public final MarkupElement nextElement() throws ParseException{
        IXmlPullParser.HttpTagType type;
        while((type=this.parser.next())!=IXmlPullParser.HttpTagType.NOT_INITIALIZED){
            if(type==IXmlPullParser.HttpTagType.BODY){
                continue;
            }
            if(type==IXmlPullParser.HttpTagType.TAG){
                return new ComponentTag(this.parser.getElement());
            }
            return new HtmlSpecialTag(this.parser.getElement(),type);
        }
        return null;
    }
    public final IMarkupFilter getNextFilter(){
        return null;
    }
    public final void setNextFilter(final IMarkupFilter parent){
        throw new IllegalArgumentException("You can not set the parent with RootMarkupFilter.");
    }
    protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        return tag;
    }
    public final void postProcess(final Markup markup){
    }
    public String toString(){
        return this.parser.toString();
    }
}
