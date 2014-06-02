package org.apache.wicket.markup;

public interface IMarkup{
    int findComponentIndex(String p0,String p1);
    MarkupElement get(int p0);
    String getEncoding();
    MarkupResourceStream getResource();
    String getWicketNamespace();
    String getXmlDeclaration();
    int size();
    String toDebugString();
    String toString();
}
