package org.apache.wicket.markup;

public final class RawMarkup extends MarkupElement{
    private final CharSequence string;
    public RawMarkup(final CharSequence string){
        super();
        this.string=string;
    }
    public boolean equals(final Object o){
        if(o instanceof CharSequence){
            return this.string.equals(o);
        }
        return o instanceof RawMarkup&&this.string.equals(((RawMarkup)o).string);
    }
    public boolean equalTo(final MarkupElement element){
        return element instanceof RawMarkup&&this.toString().equals(element.toString());
    }
    public int hashCode(){
        return this.string.hashCode();
    }
    public CharSequence toCharSequence(){
        return this.string;
    }
    public String toString(){
        return this.string.toString();
    }
    public String toUserDebugString(){
        return "[Raw markup]";
    }
}
