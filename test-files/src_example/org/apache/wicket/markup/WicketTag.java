package org.apache.wicket.markup;

import org.apache.wicket.markup.parser.*;

public class WicketTag extends ComponentTag{
    public WicketTag(final XmlTag tag){
        super(tag);
    }
    public WicketTag(final ComponentTag tag){
        super(tag.getXmlTag());
        tag.copyPropertiesTo(this);
    }
    public final String getNameAttribute(){
        return this.getAttributes().getString("name");
    }
    public final boolean isContainerTag(){
        return "container".equalsIgnoreCase(this.getName());
    }
    public final boolean isLinkTag(){
        return "link".equalsIgnoreCase(this.getName());
    }
    public final boolean isRemoveTag(){
        return "remove".equalsIgnoreCase(this.getName());
    }
    public final boolean isBodyTag(){
        return "body".equalsIgnoreCase(this.getName());
    }
    public final boolean isChildTag(){
        return "child".equalsIgnoreCase(this.getName());
    }
    public final boolean isExtendTag(){
        return "extend".equalsIgnoreCase(this.getName());
    }
    public final boolean isHeadTag(){
        return "head".equalsIgnoreCase(this.getName());
    }
    public final boolean isMessageTag(){
        return "message".equalsIgnoreCase(this.getName());
    }
    public final boolean isPanelTag(){
        return "panel".equalsIgnoreCase(this.getName());
    }
    public final boolean isBorderTag(){
        return "border".equalsIgnoreCase(this.getName());
    }
    public final boolean isFragementTag(){
        return "fragment".equalsIgnoreCase(this.getName());
    }
    public final boolean isEnclosureTag(){
        return "enclosure".equalsIgnoreCase(this.getName());
    }
    public final boolean isMajorWicketComponentTag(){
        return this.isPanelTag()||this.isBorderTag()||this.isExtendTag();
    }
    public ComponentTag mutable(){
        if(this.xmlTag.isMutable()){
            return this;
        }
        final WicketTag tag=new WicketTag(this.xmlTag.mutable());
        tag.setId(this.getId());
        tag.setAutoComponentTag(this.isAutoComponentTag());
        return tag;
    }
}
