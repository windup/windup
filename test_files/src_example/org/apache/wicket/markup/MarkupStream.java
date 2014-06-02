package org.apache.wicket.markup;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.string.*;

public class MarkupStream{
    private MarkupElement current;
    private int currentIndex;
    private final IMarkupFragment markup;
    public MarkupStream(final IMarkupFragment markup){
        super();
        this.currentIndex=0;
        Args.notNull((Object)markup,"markup");
        this.markup=markup;
        if(markup.size()>0){
            this.current=this.get(this.currentIndex);
        }
    }
    public boolean atCloseTag(){
        return this.atTag()&&this.getTag().isClose();
    }
    public boolean atOpenCloseTag(){
        return this.atTag()&&this.getTag().isOpenClose();
    }
    public boolean atOpenCloseTag(final String componentId){
        return this.atOpenCloseTag()&&componentId.equals(this.getTag().getId());
    }
    public boolean atOpenTag(){
        return this.atTag()&&this.getTag().isOpen();
    }
    public boolean atOpenTag(final String id){
        return this.atOpenTag()&&id.equals(this.getTag().getId());
    }
    public boolean atTag(){
        return this.current instanceof ComponentTag;
    }
    public boolean equalTo(final MarkupStream that){
        while(this.hasMore()){
            final MarkupElement thisElement=this.get();
            final MarkupElement thatElement=that.get();
            if(thisElement!=null&&thatElement!=null){
                if(!thisElement.equalTo(thatElement)){
                    return false;
                }
            }
            else if(thisElement!=null||thatElement!=null){
                return false;
            }
            this.next();
            that.next();
        }
        return !that.hasMore();
    }
    public final boolean equalMarkup(final MarkupStream markupStream){
        return markupStream!=null&&this.markup==markupStream.markup;
    }
    public MarkupElement get(){
        return this.current;
    }
    public MarkupElement get(final int index){
        return this.markup.get(index);
    }
    public final Class<? extends Component> getContainerClass(){
        return this.markup.getMarkupResourceStream().getMarkupClass();
    }
    public int getCurrentIndex(){
        return this.currentIndex;
    }
    public final String getEncoding(){
        return this.markup.getMarkupResourceStream().getEncoding();
    }
    public IResourceStream getResource(){
        return this.markup.getMarkupResourceStream().getResource();
    }
    public ComponentTag getTag(){
        if(this.current instanceof ComponentTag){
            return (ComponentTag)this.current;
        }
        this.throwMarkupException("Tag expected");
        return null;
    }
    public final String getWicketNamespace(){
        return this.markup.getMarkupResourceStream().getWicketNamespace();
    }
    public boolean hasMore(){
        return this.currentIndex<this.markup.size();
    }
    public final boolean isMergedMarkup(){
        return this.markup instanceof MergedMarkup;
    }
    public MarkupElement next(){
        if(++this.currentIndex<this.markup.size()){
            return this.current=this.get(this.currentIndex);
        }
        return null;
    }
    public MarkupElement nextOpenTag(){
        while(this.next()!=null){
            final MarkupElement elem=this.get();
            if(elem instanceof ComponentTag){
                final ComponentTag tag=(ComponentTag)elem;
                if(tag.isOpen()||tag.isOpenClose()){
                    return this.current=this.get(this.currentIndex);
                }
                continue;
            }
        }
        return null;
    }
    public MarkupStream setCurrentIndex(final int currentIndex){
        this.current=this.get(currentIndex);
        this.currentIndex=currentIndex;
        return this;
    }
    public final void skipComponent(){
        final ComponentTag startTag=this.getTag();
        if(startTag.isOpen()){
            if(!startTag.hasNoCloseTag()){
                this.next();
                this.skipToMatchingCloseTag(startTag);
            }
            this.next();
        }
        else if(startTag.isOpenClose()){
            this.next();
        }
        else{
            this.throwMarkupException("Skip component called on bad markup element "+startTag);
        }
    }
    public void skipRawMarkup(){
        while(true){
            if(this.current instanceof RawMarkup){
                if(this.next()!=null){
                    continue;
                }
                break;
            }
            else{
                if(!(this.current instanceof ComponentTag)||this.current instanceof WicketTag){
                    break;
                }
                final ComponentTag tag=(ComponentTag)this.current;
                if(tag.isAutoComponentTag()){
                    if(this.next()!=null){
                        continue;
                    }
                    break;
                }
                else{
                    if(tag.isClose()&&tag.getOpenTag().isAutoComponentTag()&&this.next()!=null){
                        continue;
                    }
                    break;
                }
            }
        }
    }
    public boolean skipUntil(final Class<? extends MarkupElement> clazz){
        while(this.hasMore()){
            if(clazz.isInstance(this.current)){
                return true;
            }
            this.next();
        }
        return false;
    }
    public void skipUntil(final String wicketTagName){
        while(!(this.current instanceof WicketTag)||!((WicketTag)this.current).getName().equals(wicketTagName)){
            if(this.next()==null){
                return;
            }
        }
    }
    public void skipToMatchingCloseTag(final ComponentTag openTag){
        while(this.hasMore()){
            if(this.get().closes(openTag)){
                return;
            }
            this.next();
        }
        this.throwMarkupException("Expected close tag for "+openTag);
    }
    public final IMarkupFragment getMarkupFragment(){
        return new MarkupFragment(this.markup,this.currentIndex);
    }
    public final String getTagAttribute(final String name,final boolean withWicketNamespace){
        String attr=withWicketNamespace?(attr=this.getWicketNamespace()+":"+name):name;
        return this.getTag().getAttributes().getString(attr);
    }
    public final ComponentTag getPreviousTag(){
        final MarkupElement elem=this.get(this.currentIndex-1);
        if(!(elem instanceof ComponentTag)){
            this.throwMarkupException("Tag expected");
        }
        return (ComponentTag)elem;
    }
    public void throwMarkupException(final String message){
        throw new MarkupException(this,message);
    }
    public String toHtmlDebugString(){
        final StringBuilder buffer=new StringBuilder();
        for(int i=0;i<this.markup.size();++i){
            if(i==this.currentIndex){
                buffer.append("<font color = \"red\">");
            }
            final MarkupElement element=this.markup.get(i);
            buffer.append(Strings.escapeMarkup((CharSequence)element.toString(),true).toString());
            if(i==this.currentIndex){
                buffer.append("</font>");
            }
        }
        return buffer.toString();
    }
    public String toString(){
        return "[markup = "+String.valueOf(this.markup)+", index = "+this.currentIndex+", current = "+((this.current==null)?"null":this.current.toUserDebugString())+"]";
    }
}
