package org.apache.wicket.protocol.http.documentvalidation;

public class TextContent implements DocumentElement{
    private final String value;
    public TextContent(final String value){
        super();
        this.value=value;
    }
    public String getValue(){
        return this.value;
    }
    public String toString(){
        return "[text = '"+this.value+"']";
    }
}
