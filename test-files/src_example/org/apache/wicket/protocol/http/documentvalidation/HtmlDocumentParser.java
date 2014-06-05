package org.apache.wicket.protocol.http.documentvalidation;

import org.apache.wicket.util.string.*;
import java.util.*;
import org.slf4j.*;
import java.io.*;

public class HtmlDocumentParser{
    private static final Logger log;
    public static final int CLOSE_TAG=4;
    public static final int COMMENT=1;
    public static final int END=0;
    public static final int OPEN_TAG=2;
    public static final int OPENCLOSE_TAG=3;
    public static final int TEXT=5;
    public static final int UNKNOWN=-1;
    private Map<String,String> attributes;
    private String comment;
    private final String document;
    private int pos;
    private String tag;
    private String text;
    public HtmlDocumentParser(final String document){
        super();
        CharSequence tmp=Strings.replaceAll((CharSequence)document,(CharSequence)"\n",(CharSequence)"");
        tmp=Strings.replaceAll(tmp,(CharSequence)"\r",(CharSequence)"");
        this.document=Strings.replaceAll(tmp,(CharSequence)"\t",(CharSequence)" ").toString();
        this.pos=0;
    }
    public Map<String,String> getAttributes(){
        return this.attributes;
    }
    public String getComment(){
        return this.comment;
    }
    public int getNextToken(){
        if(this.pos>=this.document.length()){
            return 0;
        }
        final char ch=this.document.charAt(this.pos);
        if(ch=='<'){
            return this.processDirective();
        }
        return this.processText();
    }
    public String getTag(){
        return this.tag;
    }
    public String getText(){
        return this.text;
    }
    private Map<String,String> extractAttributes(String attributeString){
        final Map<String,String> m=(Map<String,String>)new HashMap();
        attributeString=Strings.replaceAll((CharSequence)attributeString.trim(),(CharSequence)"\t",(CharSequence)" ").toString();
        attributeString=Strings.replaceAll((CharSequence)attributeString,(CharSequence)" = ",(CharSequence)"=").toString();
        final String[] arr$;
        final String[] attributeElements=arr$=attributeString.split(" ");
        for(final String attributeElement : arr$){
            final String[] bits=attributeElement.split("=");
            if(bits.length==1){
                m.put(bits[0].trim().toLowerCase(),"");
            }
            else{
                bits[0]=bits[0].trim();
                final StringBuilder value=new StringBuilder();
                for(int j=1;j<bits.length;++j){
                    value.append(bits[j]);
                    if(j<bits.length-1){
                        value.append('=');
                    }
                }
                bits[1]=value.toString().trim();
                if(bits[1].startsWith("\"")){
                    bits[1]=bits[1].substring(1);
                }
                if(bits[1].endsWith("\"")){
                    bits[1]=bits[1].substring(0,bits[1].length()-1);
                }
                m.put(bits[0].toLowerCase(),bits[1]);
            }
        }
        return m;
    }
    private int processDirective(){
        final String part=this.document.substring(this.pos);
        if(part.matches("<!--.*-->.*")){
            this.comment=part.substring(4,part.indexOf("-->")).trim();
            this.pos+=part.indexOf("-->")+3;
            return 1;
        }
        if(part.matches("</.*>.*")){
            this.tag=part.substring(2,part.indexOf(62)).trim().toLowerCase();
            this.pos+=part.indexOf(">")+1;
            return 4;
        }
        if(part.matches("<[^/]+[^>]*/>.*")){
            if(part.matches("<([a-zA-Z]+:)?[a-zA-Z]+/>.*")){
                this.tag=part.substring(1,part.indexOf("/>")).toLowerCase();
                this.attributes=(Map<String,String>)new HashMap();
            }
            else{
                this.tag=part.substring(1,part.indexOf(32)).toLowerCase();
                final String attributeString=part.substring(part.indexOf(32),part.indexOf("/>"));
                this.attributes=this.extractAttributes(attributeString);
            }
            this.pos+=part.indexOf("/>")+2;
            return 3;
        }
        if(part.matches("<[^/>]+.*>.*")){
            if(part.matches("<([a-zA-Z]+:)?[a-zA-Z0-9]*>.*")){
                this.tag=part.substring(1,part.indexOf(62)).toLowerCase();
                this.attributes=(Map<String,String>)new HashMap();
            }
            else{
                this.tag=part.substring(1,part.indexOf(32)).toLowerCase();
                final String attributeString=part.substring(part.indexOf(32),part.indexOf(62));
                this.attributes=this.extractAttributes(attributeString);
            }
            this.pos+=part.indexOf(">")+1;
            return 2;
        }
        final int size=(part.length()>30)?30:part.length();
        HtmlDocumentParser.log.error("Unexpected markup found: "+part.substring(0,size)+"...");
        return -1;
    }
    private int processText(){
        final StringBuilder buf=new StringBuilder();
        while(this.pos<this.document.length()){
            final char ch=this.document.charAt(this.pos);
            if(ch=='<'){
                this.text=buf.toString();
                return 5;
            }
            buf.append(ch);
            ++this.pos;
        }
        if(buf.length()>0){
            this.text=buf.toString();
            return 5;
        }
        return 0;
    }
    static{
        log=LoggerFactory.getLogger(HtmlDocumentParser.class);
    }
}
