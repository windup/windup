package org.apache.wicket.markup.html;

import java.io.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.request.*;

public interface IHeaderResponse extends Closeable{
    void renderJavaScriptReference(ResourceReference p0);
    void renderJavaScriptReference(ResourceReference p0,String p1);
    void renderJavaScriptReference(ResourceReference p0,PageParameters p1,String p2);
    void renderJavaScriptReference(ResourceReference p0,PageParameters p1,String p2,boolean p3);
    void renderJavaScriptReference(ResourceReference p0,PageParameters p1,String p2,boolean p3,String p4);
    void renderJavaScriptReference(String p0);
    void renderJavaScriptReference(String p0,String p1);
    void renderJavaScriptReference(String p0,String p1,boolean p2);
    void renderJavaScriptReference(String p0,String p1,boolean p2,String p3);
    void renderJavaScript(CharSequence p0,String p1);
    void renderCSS(CharSequence p0,String p1);
    void renderCSSReference(ResourceReference p0);
    void renderCSSReference(String p0);
    void renderCSSReference(ResourceReference p0,String p1);
    void renderCSSReference(ResourceReference p0,PageParameters p1,String p2);
    void renderCSSReference(ResourceReference p0,PageParameters p1,String p2,String p3);
    void renderCSSReference(String p0,String p1);
    void renderCSSReference(String p0,String p1,String p2);
    void renderString(CharSequence p0);
    void markRendered(Object p0);
    boolean wasRendered(Object p0);
    Response getResponse();
    void renderOnDomReadyJavaScript(String p0);
    void renderOnLoadJavaScript(String p0);
    void renderOnEventJavaScript(String p0,String p1,String p2);
    void close();
    boolean isClosed();
}
