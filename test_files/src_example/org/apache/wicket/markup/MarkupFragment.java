package org.apache.wicket.markup;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.markup.parser.filter.*;
import org.apache.wicket.util.string.*;
import java.util.*;

public class MarkupFragment implements IMarkupFragment{
    private final IMarkupFragment markup;
    private final int startIndex;
    private final int size;
    public MarkupFragment(final IMarkupFragment markup,final int startIndex){
        super();
        Args.notNull((Object)markup,"markup");
        if(startIndex<0){
            throw new IllegalArgumentException("Parameter 'startIndex' must not be < 0");
        }
        final int markupSize=markup.size();
        if(startIndex>=markupSize){
            throw new IllegalArgumentException("Parameter 'startIndex' must not be >= markup.size()");
        }
        this.markup=markup;
        this.startIndex=startIndex;
        final MarkupElement startElem=markup.get(startIndex);
        if(!(startElem instanceof ComponentTag)){
            throw new IllegalArgumentException("Parameter 'startIndex' does not point to a Wicket open tag");
        }
        final ComponentTag startTag=(ComponentTag)startElem;
        int endIndex;
        if(startTag.isOpenClose()){
            endIndex=startIndex;
        }
        else if(startTag.hasNoCloseTag()){
            if(!HtmlHandler.requiresCloseTag(startTag.getName())){
                endIndex=startIndex;
            }
            else{
                endIndex=markupSize;
            }
        }
        else{
            for(endIndex=startIndex+1;endIndex<markupSize;++endIndex){
                final MarkupElement elem=markup.get(endIndex);
                if(elem instanceof ComponentTag){
                    final ComponentTag tag=(ComponentTag)elem;
                    if(tag.closes(startTag)){
                        break;
                    }
                }
            }
        }
        if(endIndex>=markupSize){
            throw new MarkupException("Unable to find close tag for: '"+startTag.toString()+"' in "+this.getRootMarkup().getMarkupResourceStream().toString());
        }
        this.size=endIndex-startIndex+1;
    }
    public final MarkupElement get(final int index){
        if(index<0||index>this.size){
            throw new IndexOutOfBoundsException("Parameter 'index' is out of range: 0 <= "+index+" <= "+this.size);
        }
        return this.markup.get(this.startIndex+index);
    }
    public final IMarkupFragment find(final String id){
        Args.notEmpty((CharSequence)id,"id");
        final MarkupStream stream=new MarkupStream(this);
        stream.setCurrentIndex(1);
        while(stream.hasMore()){
            final MarkupElement elem=stream.get();
            if(elem instanceof ComponentTag){
                final ComponentTag tag=stream.getTag();
                if(tag.isOpen()||tag.isOpenClose()){
                    if(tag.getId().equals(id)){
                        return stream.getMarkupFragment();
                    }
                    if(tag.isOpen()&&!tag.hasNoCloseTag()&&!tag.isAutoComponentTag()){
                        stream.skipToMatchingCloseTag(tag);
                    }
                }
            }
            stream.next();
        }
        return null;
    }
    public final MarkupResourceStream getMarkupResourceStream(){
        return this.markup.getMarkupResourceStream();
    }
    public final int size(){
        return this.size;
    }
    private final IMarkupFragment getParentMarkup(){
        return this.markup;
    }
    public final Markup getRootMarkup(){
        IMarkupFragment markup;
        for(markup=this.getParentMarkup();markup!=null&&!(markup instanceof Markup);markup=((MarkupFragment)markup).getParentMarkup()){
        }
        return (Markup)markup;
    }
    public String toString(){
        return this.toString(false);
    }
    public String toString(final boolean markupOnly){
        final AppendingStringBuffer buf=new AppendingStringBuffer(400);
        if(!markupOnly){
            buf.append(this.getRootMarkup().getMarkupResourceStream().toString());
            buf.append("\n");
        }
        for(int i=0;i<this.size();++i){
            buf.append((Object)this.get(i));
        }
        return buf.toString();
    }
    public Iterator<MarkupElement> iterator(){
        return this.getRootMarkup().iterator(this.startIndex,this.size);
    }
}
