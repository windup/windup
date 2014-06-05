package org.apache.wicket.markup;

import java.text.*;
import org.apache.wicket.markup.parser.*;

public class WicketParseException extends ParseException{
    private static final long serialVersionUID=1L;
    public WicketParseException(final String message,final XmlTag tag){
        super(message+tag.toUserDebugString(),tag.getPos());
    }
    public WicketParseException(final String message,final ComponentTag tag){
        this(message,tag.xmlTag);
    }
}
