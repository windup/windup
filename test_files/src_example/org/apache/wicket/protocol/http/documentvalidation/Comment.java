package org.apache.wicket.protocol.http.documentvalidation;

public class Comment implements DocumentElement{
    private final String text;
    public Comment(final String text){
        super();
        this.text=text;
    }
    public String getText(){
        return this.text;
    }
    public String toString(){
        return "[comment = '"+this.text+"']";
    }
}
