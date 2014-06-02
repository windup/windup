package org.apache.wicket.markup;

import org.apache.wicket.markup.parser.*;
import org.apache.wicket.request.*;
import org.apache.wicket.*;

public class HtmlSpecialTag extends MarkupElement{
    protected final XmlTag xmlTag;
    private int flags;
    private final IXmlPullParser.HttpTagType httpTagType;
    public HtmlSpecialTag(final XmlTag tag,final IXmlPullParser.HttpTagType httpTagType){
        super();
        this.flags=0;
        this.xmlTag=tag.makeImmutable();
        this.httpTagType=httpTagType;
    }
    public final void setFlag(final int flag,final boolean set){
        if(set){
            this.flags|=flag;
        }
        else{
            this.flags&=~flag;
        }
    }
    public final boolean getFlag(final int flag){
        return (this.flags&flag)!=0x0;
    }
    public final int getLength(){
        return this.xmlTag.getLength();
    }
    public final int getPos(){
        return this.xmlTag.getPos();
    }
    public final XmlTag.TagType getType(){
        return this.xmlTag.getType();
    }
    public final boolean isClose(){
        return this.xmlTag.isClose();
    }
    public final boolean isOpen(){
        return this.xmlTag.isOpen();
    }
    public final boolean isOpenClose(){
        return this.xmlTag.isOpenClose();
    }
    void copyPropertiesTo(final HtmlSpecialTag dest){
        dest.flags=this.flags;
    }
    public CharSequence toCharSequence(){
        return this.xmlTag.toCharSequence();
    }
    public final String toString(){
        return ""+this.httpTagType+": '"+this.xmlTag.toString()+"'";
    }
    public final void writeOutput(final Response response,final boolean stripWicketAttributes,final String namespace){
        response.write((CharSequence)this.toString());
    }
    public final String toUserDebugString(){
        return this.xmlTag.toUserDebugString();
    }
    public final XmlTag getXmlTag(){
        return this.xmlTag;
    }
    public boolean equalTo(final MarkupElement element){
        if(element instanceof HtmlSpecialTag){
            final HtmlSpecialTag that=(HtmlSpecialTag)element;
            return this.getXmlTag().equalTo(that.getXmlTag());
        }
        return false;
    }
    public void onBeforeRender(final Component component,final MarkupStream markupStream){
    }
    public final IXmlPullParser.HttpTagType getHttpTagType(){
        return this.httpTagType;
    }
}
