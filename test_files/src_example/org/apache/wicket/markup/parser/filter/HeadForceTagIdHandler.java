package org.apache.wicket.markup.parser.filter;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.markup.*;
import java.text.*;

public class HeadForceTagIdHandler extends AbstractMarkupFilter{
    private final String headElementIdPrefix;
    private int counter;
    private boolean inHead;
    public HeadForceTagIdHandler(final Class<?> markupFileClass){
        super();
        this.counter=0;
        this.inHead=false;
        final AppendingStringBuffer buffer=new AppendingStringBuffer((CharSequence)markupFileClass.getName());
        for(int i=0;i<buffer.getValue().length;++i){
            if(!Character.isLetterOrDigit(buffer.getValue()[i])){
                buffer.getValue()[i]='-';
            }
        }
        buffer.append("-");
        this.headElementIdPrefix=buffer.toString();
    }
    protected final MarkupElement onComponentTag(final ComponentTag tag) throws ParseException{
        if(tag instanceof WicketTag&&((WicketTag)tag).isHeadTag()){
            this.inHead=tag.isOpen();
        }
        else if(this.inHead&&!(tag instanceof WicketTag)&&tag.getId()==null&&(tag.isOpen()||tag.isOpenClose())&&this.needId(tag)&&tag.getAttributes().get((Object)"id")==null){
            tag.getAttributes().put((Object)"id",(Object)(this.headElementIdPrefix+this.nextValue()));
            tag.setModified(true);
        }
        return tag;
    }
    private final boolean needId(final ComponentTag tag){
        final String name=tag.getName().toLowerCase();
        return (name.equals("script")&&!tag.getAttributes().containsKey((Object)"src"))||(name.equals("style")&&!tag.getAttributes().containsKey((Object)"href"));
    }
    private final int nextValue(){
        return this.counter++;
    }
}
