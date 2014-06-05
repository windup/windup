package org.apache.wicket.markup;

public abstract class MarkupElement{
    public boolean closes(final MarkupElement open){
        return false;
    }
    public abstract boolean equalTo(final MarkupElement p0);
    public abstract CharSequence toCharSequence();
    public abstract String toUserDebugString();
}
