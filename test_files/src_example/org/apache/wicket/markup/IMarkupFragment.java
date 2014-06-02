package org.apache.wicket.markup;

public interface IMarkupFragment extends Iterable<MarkupElement>{
    MarkupElement get(int p0);
    MarkupResourceStream getMarkupResourceStream();
    int size();
    IMarkupFragment find(String p0);
    String toString(boolean p0);
}
