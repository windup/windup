package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.util.collections.*;
import org.apache.wicket.markup.*;
import java.text.*;
import org.slf4j.*;
import java.util.*;

public final class HtmlHandler extends AbstractMarkupFilter{
    private static final Logger log;
    private final ArrayListStack<ComponentTag> stack;
    private static final Map<String,Boolean> doesNotRequireCloseTag;
    public HtmlHandler(){
        super();
        this.stack=(ArrayListStack<ComponentTag>)new ArrayListStack();
    }
    public void postProcess(final Markup markup){
        while(this.stack.size()>0){
            final ComponentTag top=(ComponentTag)this.stack.peek();
            if(requiresCloseTag(top.getName())){
                throw new MarkupException(markup,"Tag does not have a close tag",null);
            }
            this.stack.pop();
            top.setHasNoCloseTag(true);
        }
    }
    protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag.isOpen()){
            this.stack.push((Object)tag);
        }
        else if(tag.isClose()){
            if(this.stack.size()<=0){
                throw new WicketParseException("Tag does not have a matching open tag:",tag);
            }
            ComponentTag top=(ComponentTag)this.stack.pop();
            boolean mismatch=!hasEqualTagName(top,tag);
            if(mismatch){
                top.setHasNoCloseTag(true);
                while(mismatch&&!requiresCloseTag(top.getName())){
                    top.setHasNoCloseTag(true);
                    if(this.stack.isEmpty()){
                        break;
                    }
                    top=(ComponentTag)this.stack.pop();
                    mismatch=!hasEqualTagName(top,tag);
                }
                if(mismatch){
                    throw new ParseException("Tag "+top.toUserDebugString()+" has a mismatched close tag at "+tag.toUserDebugString(),top.getPos());
                }
            }
            tag.setOpenTag(top);
        }
        else if(tag.isOpenClose()){
            tag.setOpenTag(tag);
        }
        return tag;
    }
    public static boolean requiresCloseTag(final String name){
        return HtmlHandler.doesNotRequireCloseTag.get(name.toLowerCase())==null;
    }
    public static boolean hasEqualTagName(final ComponentTag tag1,final ComponentTag tag2){
        return tag1.getName().equalsIgnoreCase(tag2.getName())&&((tag1.getNamespace()==null&&tag2.getNamespace()==null)||(tag1.getNamespace()!=null&&tag2.getNamespace()!=null&&tag1.getNamespace().equalsIgnoreCase(tag2.getNamespace())));
    }
    static{
        log=LoggerFactory.getLogger(HtmlHandler.class);
        (doesNotRequireCloseTag=new HashMap()).put("p",Boolean.TRUE);
        HtmlHandler.doesNotRequireCloseTag.put("br",Boolean.TRUE);
        HtmlHandler.doesNotRequireCloseTag.put("img",Boolean.TRUE);
        HtmlHandler.doesNotRequireCloseTag.put("input",Boolean.TRUE);
        HtmlHandler.doesNotRequireCloseTag.put("hr",Boolean.TRUE);
        HtmlHandler.doesNotRequireCloseTag.put("link",Boolean.TRUE);
        HtmlHandler.doesNotRequireCloseTag.put("meta",Boolean.TRUE);
    }
}
