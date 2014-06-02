package org.apache.wicket.util.string;

import org.apache.wicket.request.*;

public class JavaScriptUtils{
    public static final String SCRIPT_CONTENT_PREFIX="\n/*<![CDATA[*/\n";
    public static final String SCRIPT_CONTENT_SUFFIX="\n/*]]>*/\n";
    public static final String SCRIPT_OPEN_TAG="<script type=\"text/javascript\">\n/*<![CDATA[*/\n";
    public static final String SCRIPT_CLOSE_TAG="\n/*]]>*/\n</script>\n";
    private final Response response;
    public JavaScriptUtils(final Response response,final String id){
        super();
        writeOpenTag(this.response=response,id);
    }
    public JavaScriptUtils(final Response response){
        super();
        writeOpenTag(this.response=response);
    }
    public static CharSequence escapeQuotes(final CharSequence input){
        CharSequence s=input;
        if(s!=null){
            s=Strings.replaceAll(s,(CharSequence)"'",(CharSequence)"\\'");
            s=Strings.replaceAll(s,(CharSequence)"\"",(CharSequence)"\\\"");
        }
        return s;
    }
    public static void writeJavaScriptUrl(final Response response,final CharSequence url,final String id){
        writeJavaScriptUrl(response,url,id,false,null);
    }
    public static void writeJavaScriptUrl(final Response response,final CharSequence url,final String id,final boolean defer,final String charset){
        response.write((CharSequence)"<script type=\"text/javascript\" ");
        if(id!=null){
            response.write((CharSequence)("id=\""+(Object)Strings.escapeMarkup((CharSequence)id)+"\" "));
        }
        if(defer){
            response.write((CharSequence)"defer=\"defer\" ");
        }
        if(charset!=null){
            response.write((CharSequence)("charset=\""+(Object)Strings.escapeMarkup((CharSequence)charset)+"\" "));
        }
        response.write((CharSequence)"src=\"");
        response.write(url);
        response.write((CharSequence)"\"></script>");
        response.write((CharSequence)"\n");
    }
    public static void writeJavaScriptUrl(final Response response,final CharSequence url){
        writeJavaScriptUrl(response,url,null);
    }
    public static void writeJavaScript(final Response response,final CharSequence text,final String id){
        writeOpenTag(response,id);
        response.write(Strings.replaceAll(text,(CharSequence)"</",(CharSequence)"<\\/"));
        writeCloseTag(response);
    }
    public static void writeJavaScript(final Response response,final CharSequence text){
        writeJavaScript(response,text,null);
    }
    public static void writeOpenTag(final Response response,final String id){
        response.write((CharSequence)"<script type=\"text/javascript\" ");
        if(id!=null){
            response.write((CharSequence)("id=\""+(Object)Strings.escapeMarkup((CharSequence)id)+"\""));
        }
        response.write((CharSequence)">");
        response.write((CharSequence)"\n/*<![CDATA[*/\n");
    }
    public static void writeOpenTag(final Response response){
        writeOpenTag(response,null);
    }
    public static void writeCloseTag(final Response response){
        response.write((CharSequence)"\n/*]]>*/\n");
        response.write((CharSequence)"</script>\n");
    }
    public void write(final CharSequence script){
        this.response.write(script);
    }
    public void println(final CharSequence script){
        this.response.write(script);
    }
    public void close(){
        writeCloseTag(this.response);
    }
}
