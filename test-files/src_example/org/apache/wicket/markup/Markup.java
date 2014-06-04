package org.apache.wicket.markup;

import java.io.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class Markup implements IMarkupFragment{
    private static final Logger log;
    public static final Markup NO_MARKUP;
    private List<MarkupElement> markupElements;
    private final MarkupResourceStream markupResourceStream;
    public static Markup of(final String markup){
        try{
            return new MarkupParser(markup).parse();
        }
        catch(IOException ex){
            throw new RuntimeException((Throwable)ex);
        }
        catch(ResourceStreamNotFoundException ex2){
            throw new RuntimeException((Throwable)ex2);
        }
    }
    private Markup(){
        super();
        this.markupResourceStream=null;
    }
    public Markup(final MarkupResourceStream markupResourceStream){
        super();
        if(markupResourceStream==null){
            throw new IllegalArgumentException("Parameter 'markupResourceStream' must not be null");
        }
        this.markupResourceStream=markupResourceStream;
        this.markupElements=(List<MarkupElement>)new ArrayList();
    }
    public final MarkupElement get(final int index){
        return (MarkupElement)this.markupElements.get(index);
    }
    public final MarkupResourceStream getMarkupResourceStream(){
        return this.markupResourceStream;
    }
    public final void replace(final int index,final MarkupElement elem){
        Args.notNull((Object)elem,"elem");
        if(index<0||index>=this.size()){
            throw new IndexOutOfBoundsException("'index' must be smaller than size(). Index:"+index+"; size:"+this.size());
        }
        this.markupElements.set(index,elem);
    }
    public String locationAsString(){
        return this.markupResourceStream.locationAsString();
    }
    public final int size(){
        return this.markupElements.size();
    }
    public final void addMarkupElement(final MarkupElement markupElement){
        this.markupElements.add(markupElement);
    }
    public final void addMarkupElement(final int pos,final MarkupElement markupElement){
        this.markupElements.add(pos,markupElement);
    }
    public final void makeImmutable(){
        for(final MarkupElement markupElement : this.markupElements){
            if(markupElement instanceof ComponentTag){
                ((ComponentTag)markupElement).makeImmutable();
            }
        }
        this.markupElements=(List<MarkupElement>)Collections.unmodifiableList(this.markupElements);
    }
    public final IMarkupFragment find(final String id){
        Args.notEmpty((CharSequence)id,"id");
        final MarkupStream stream=new MarkupStream(this);
        stream.setCurrentIndex(0);
        while(stream.hasMore()){
            final MarkupElement elem=stream.get();
            if(elem instanceof ComponentTag){
                final ComponentTag tag=stream.getTag();
                if(tag.isOpen()||tag.isOpenClose()){
                    if(tag.getId().equals(id)){
                        return stream.getMarkupFragment();
                    }
                    if(tag.isOpen()&&!tag.hasNoCloseTag()&&!(tag instanceof WicketTag)&&!"head".equals(tag.getName())&&!tag.isAutoComponentTag()){
                        stream.skipToMatchingCloseTag(tag);
                    }
                }
            }
            stream.next();
        }
        return null;
    }
    public final String toString(){
        return this.toString(false);
    }
    public final String toString(final boolean markupOnly){
        final AppendingStringBuffer buf=new AppendingStringBuffer(400);
        if(!markupOnly){
            if(this.markupResourceStream!=null){
                buf.append(this.markupResourceStream.toString());
            }
            else{
                buf.append("null MarkupResouceStream");
            }
            buf.append("\n");
        }
        if(this.markupElements!=null){
            for(final MarkupElement markupElement : this.markupElements){
                buf.append((Object)markupElement);
            }
        }
        return buf.toString();
    }
    public final Iterator<MarkupElement> iterator(){
        return (Iterator<MarkupElement>)this.markupElements.iterator();
    }
    public final Iterator<MarkupElement> iterator(final int startIndex,final int size){
        return (Iterator<MarkupElement>)this.markupElements.subList(startIndex,startIndex+size).iterator();
    }
    static{
        log=LoggerFactory.getLogger(Markup.class);
        NO_MARKUP=new Markup();
    }
}
