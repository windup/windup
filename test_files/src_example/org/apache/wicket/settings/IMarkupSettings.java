package org.apache.wicket.settings;

import org.apache.wicket.markup.*;

public interface IMarkupSettings{
    boolean getAutomaticLinking();
    boolean getCompressWhitespace();
    String getDefaultAfterDisabledLink();
    String getDefaultBeforeDisabledLink();
    String getDefaultMarkupEncoding();
    boolean getStripComments();
    boolean getStripWicketTags();
    boolean getThrowExceptionOnMissingXmlDeclaration();
    void setAutomaticLinking(boolean p0);
    void setCompressWhitespace(boolean p0);
    void setDefaultAfterDisabledLink(String p0);
    void setDefaultBeforeDisabledLink(String p0);
    void setDefaultMarkupEncoding(String p0);
    void setStripComments(boolean p0);
    void setStripWicketTags(boolean p0);
    void setThrowExceptionOnMissingXmlDeclaration(boolean p0);
    MarkupFactory getMarkupFactory();
    void setMarkupFactory(MarkupFactory p0);
}
