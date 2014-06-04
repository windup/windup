package org.apache.wicket.markup.parser.filter;

import java.util.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.parser.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.slf4j.*;

public class ConditionalCommentFilter extends AbstractMarkupFilter{
    private static final Logger log;
    private Queue<ComponentTag> queue;
    private Map<Integer,Set<String>> skipByLevelMap;
    private int level;
    public ConditionalCommentFilter(){
        super();
        this.queue=(Queue<ComponentTag>)new LinkedList();
        this.skipByLevelMap=(Map<Integer,Set<String>>)Generics.newHashMap();
    }
    protected MarkupElement onSpecialTag(final HtmlSpecialTag tag) throws ParseException{
        if(tag.getHttpTagType()==IXmlPullParser.HttpTagType.CONDITIONAL_COMMENT){
            return tag;
        }
        return tag;
    }
    public void postProcess(final Markup markup){
    }
    protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        return tag;
    }
    static{
        log=LoggerFactory.getLogger(ConditionalCommentFilter.class);
    }
}
