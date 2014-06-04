package org.apache.wicket.markup.parser;

import org.apache.wicket.util.collections.*;
import java.text.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.markup.parser.filter.*;
import org.slf4j.*;
import java.util.*;

public class TagStack{
    private static final Logger log;
    private static final Map<String,Boolean> doesNotRequireCloseTag;
    private final ArrayListStack<ComponentTag> stack;
    private boolean debug;
    public TagStack(){
        super();
        this.stack=(ArrayListStack<ComponentTag>)new ArrayListStack();
    }
    public void assertValidInStack(final ComponentTag tag) throws ParseException{
        if(tag==null){
            this.validate();
            return;
        }
        if(TagStack.log.isDebugEnabled()&&this.debug){
            TagStack.log.debug("tag: "+tag.toUserDebugString()+", stack: "+this.stack);
        }
        if(tag.isOpen()){
            this.stack.push((Object)tag);
        }
        else if(tag.isClose()){
            this.assertOpenTagFor(tag);
        }
        else if(tag.isOpenClose()){
            tag.setOpenTag(tag);
        }
    }
    private void assertOpenTagFor(final ComponentTag closeTag) throws ParseException{
        if(this.stack.size()>0){
            ComponentTag top=(ComponentTag)this.stack.pop();
            boolean mismatch=!hasEqualTagName(top,closeTag);
            if(mismatch){
                top.setHasNoCloseTag(true);
                while(mismatch&&!requiresCloseTag(top.getName())){
                    top.setHasNoCloseTag(true);
                    if(this.stack.isEmpty()){
                        break;
                    }
                    top=(ComponentTag)this.stack.pop();
                    mismatch=!hasEqualTagName(top,closeTag);
                }
                if(mismatch){
                    throw new ParseException("Tag "+top.toUserDebugString()+" has a mismatched close tag at "+closeTag.toUserDebugString(),top.getPos());
                }
            }
            closeTag.setOpenTag(top);
            return;
        }
        throw new WicketParseException("Tag does not have a matching open tag:",closeTag);
    }
    private void validate() throws WicketParseException{
        final ComponentTag top=this.getNotClosedTag();
        if(top!=null){
            throw new WicketParseException("Tag does not have a close tag:",top);
        }
    }
    public ComponentTag getNotClosedTag(){
        if(this.stack.size()>0){
            for(int i=0;i<this.stack.size();++i){
                final ComponentTag tag=(ComponentTag)this.stack.get(i);
                if(requiresCloseTag(tag.getName())){
                    return tag;
                }
                this.stack.pop();
            }
        }
        return null;
    }
    public void debug(){
        this.debug=true;
    }
    public static boolean requiresCloseTag(final String name){
        return TagStack.doesNotRequireCloseTag.get(name.toLowerCase())==null;
    }
    public static boolean hasEqualTagName(final ComponentTag tag1,final ComponentTag tag2){
        return tag1.getName().equalsIgnoreCase(tag2.getName())&&((tag1.getNamespace()==null&&tag2.getNamespace()==null)||(tag1.getNamespace()!=null&&tag2.getNamespace()!=null&&tag1.getNamespace().equalsIgnoreCase(tag2.getNamespace())));
    }
    static{
        log=LoggerFactory.getLogger(HtmlHandler.class);
        (doesNotRequireCloseTag=new HashMap()).put("p",Boolean.TRUE);
        TagStack.doesNotRequireCloseTag.put("br",Boolean.TRUE);
        TagStack.doesNotRequireCloseTag.put("img",Boolean.TRUE);
        TagStack.doesNotRequireCloseTag.put("input",Boolean.TRUE);
        TagStack.doesNotRequireCloseTag.put("hr",Boolean.TRUE);
        TagStack.doesNotRequireCloseTag.put("link",Boolean.TRUE);
        TagStack.doesNotRequireCloseTag.put("meta",Boolean.TRUE);
    }
}
