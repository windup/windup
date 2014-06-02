package org.apache.wicket.markup.parser;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.value.*;
import java.util.*;
import org.apache.wicket.util.string.*;
import org.slf4j.*;

public class XmlTag{
    private static final Logger log;
    TextSegment text;
    private IValueMap attributes;
    String name;
    String namespace;
    TagType type;
    private XmlTag closes;
    private XmlTag copyOf;
    private boolean isMutable;
    private IXmlPullParser.HttpTagType httpTagType;
    public XmlTag(){
        super();
        this.copyOf=this;
        this.isMutable=true;
    }
    public XmlTag(final TextSegment text,final TagType type){
        super();
        this.copyOf=this;
        this.isMutable=true;
        this.text=text;
        this.type=type;
    }
    public final boolean closes(final XmlTag open){
        return this.closes==open||(this.closes==open.copyOf&&this!=open);
    }
    public final boolean equalTo(final XmlTag element){
        return Objects.equal((Object)this.getNamespace(),(Object)element.getNamespace())&&this.getName().equals(element.getName())&&this.getAttributes().equals(element.getAttributes());
    }
    public IValueMap getAttributes(){
        if(this.attributes==null){
            if(this.copyOf==this||this.copyOf==null||this.copyOf.attributes==null){
                this.attributes=(IValueMap)new ValueMap();
            }
            else{
                this.attributes=(IValueMap)new ValueMap((Map)this.copyOf.attributes);
            }
        }
        return this.attributes;
    }
    public boolean hasAttributes(){
        return this.attributes!=null&&this.attributes.size()>0;
    }
    public int getColumnNumber(){
        return (this.text!=null)?this.text.columnNumber:0;
    }
    public int getLength(){
        return (this.text!=null)?this.text.text.length():0;
    }
    public int getLineNumber(){
        return (this.text!=null)?this.text.lineNumber:0;
    }
    public String getName(){
        return this.name;
    }
    public String getNamespace(){
        return this.namespace;
    }
    public final XmlTag getOpenTag(){
        return this.closes;
    }
    public int getPos(){
        return (this.text!=null)?this.text.pos:0;
    }
    public CharSequence getAttribute(final String key){
        return this.getAttributes().getCharSequence(key);
    }
    public TagType getType(){
        return this.type;
    }
    public boolean isClose(){
        return this.type==TagType.CLOSE;
    }
    public final boolean isMutable(){
        return this.isMutable;
    }
    public boolean isOpen(){
        return this.type==TagType.OPEN;
    }
    public boolean isOpenClose(){
        return this.type==TagType.OPEN_CLOSE;
    }
    public XmlTag makeImmutable(){
        if(this.isMutable){
            this.isMutable=false;
            if(this.attributes!=null){
                this.attributes.makeImmutable();
                this.text=null;
            }
        }
        return this;
    }
    public XmlTag mutable(){
        if(this.isMutable){
            return this;
        }
        final XmlTag tag=new XmlTag();
        this.copyPropertiesTo(tag);
        return tag;
    }
    void copyPropertiesTo(final XmlTag dest){
        dest.namespace=this.namespace;
        dest.name=this.name;
        dest.text=this.text;
        dest.type=this.type;
        dest.isMutable=true;
        dest.closes=this.closes;
        dest.copyOf=this.copyOf;
        if(this.attributes!=null){
            dest.attributes=(IValueMap)new ValueMap((Map)this.attributes);
        }
    }
    public Object put(final String key,final boolean value){
        return this.put(key,(CharSequence)Boolean.toString(value));
    }
    public Object put(final String key,final int value){
        return this.put(key,(CharSequence)Integer.toString(value));
    }
    public Object put(final String key,final CharSequence value){
        return this.getAttributes().put((Object)key,(Object)value);
    }
    public Object put(final String key,final StringValue value){
        return this.getAttributes().put((Object)key,(Object)((value!=null)?value.toString():null));
    }
    public void putAll(final Map<String,Object> map){
        for(final Map.Entry<String,Object> entry : map.entrySet()){
            final Object value=entry.getValue();
            this.put((String)entry.getKey(),(CharSequence)((value!=null)?value.toString():null));
        }
    }
    public void remove(final String key){
        this.getAttributes().remove((Object)key);
    }
    public void setName(final String name){
        if(this.isMutable){
            this.name=name;
            return;
        }
        throw new UnsupportedOperationException("Attempt to set name of immutable tag");
    }
    public void setNamespace(final String namespace){
        if(this.isMutable){
            this.namespace=namespace;
            return;
        }
        throw new UnsupportedOperationException("Attempt to set namespace of immutable tag");
    }
    public void setOpenTag(final XmlTag tag){
        this.closes=tag;
    }
    public void setType(final TagType type){
        if(this.isMutable){
            this.type=type;
            return;
        }
        throw new UnsupportedOperationException("Attempt to set type of immutable tag");
    }
    public String toDebugString(){
        return "[Tag name = "+this.name+", pos = "+this.text.pos+", line = "+this.text.lineNumber+", attributes = ["+this.getAttributes()+"], type = "+this.type+"]";
    }
    public String toString(){
        return this.toCharSequence().toString();
    }
    public CharSequence toCharSequence(){
        if(!this.isMutable&&this.text!=null){
            return this.text.text;
        }
        return this.toXmlString(null);
    }
    public String toUserDebugString(){
        return " '"+this.toString()+"' (line "+this.getLineNumber()+", column "+this.getColumnNumber()+")";
    }
    public CharSequence toXmlString(final String attributeToBeIgnored){
        final AppendingStringBuffer buffer=new AppendingStringBuffer();
        buffer.append('<');
        if(this.type==TagType.CLOSE){
            buffer.append('/');
        }
        if(this.namespace!=null){
            buffer.append(this.namespace);
            buffer.append(':');
        }
        buffer.append(this.name);
        final IValueMap attributes=this.getAttributes();
        if(attributes.size()>0){
            for(final String key : attributes.keySet()){
                if(key!=null&&(attributeToBeIgnored==null||!key.equalsIgnoreCase(attributeToBeIgnored))){
                    buffer.append(" ");
                    buffer.append(key);
                    CharSequence value=this.getAttribute(key);
                    if(value==null){
                        continue;
                    }
                    buffer.append("=\"");
                    value=Strings.escapeMarkup(value);
                    buffer.append((Object)value);
                    buffer.append("\"");
                }
            }
        }
        if(this.type==TagType.OPEN_CLOSE){
            buffer.append('/');
        }
        buffer.append('>');
        return (CharSequence)buffer;
    }
    static{
        log=LoggerFactory.getLogger(XmlTag.class);
    }
    public enum TagType{
        CLOSE("CLOSE"),OPEN("OPEN"),OPEN_CLOSE("OPEN_CLOSE");
        private String name;
        private TagType(final String name){
            this.name=name;
        }
    }
    static class TextSegment{
        final int columnNumber;
        final int lineNumber;
        final int pos;
        final CharSequence text;
        TextSegment(final CharSequence text,final int pos,final int line,final int col){
            super();
            this.text=text;
            this.pos=pos;
            this.lineNumber=line;
            this.columnNumber=col;
        }
        public final CharSequence getText(){
            return this.text;
        }
        public String toString(){
            return this.text.toString();
        }
    }
}
