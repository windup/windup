package org.apache.commons.lang.text;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.FormatFactory;
import java.util.Iterator;
import java.text.Format;
import java.util.Collection;
import org.apache.commons.lang.Validate;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.text.MessageFormat;

public class ExtendedMessageFormat extends MessageFormat{
    private static final long serialVersionUID=-2362048321261811743L;
    private static final String DUMMY_PATTERN="";
    private static final String ESCAPED_QUOTE="''";
    private static final char START_FMT=',';
    private static final char END_FE='}';
    private static final char START_FE='{';
    private static final char QUOTE='\'';
    private String toPattern;
    private final Map registry;
    public ExtendedMessageFormat(final String pattern){
        this(pattern,Locale.getDefault());
    }
    public ExtendedMessageFormat(final String pattern,final Locale locale){
        this(pattern,locale,null);
    }
    public ExtendedMessageFormat(final String pattern,final Map registry){
        this(pattern,Locale.getDefault(),registry);
    }
    public ExtendedMessageFormat(final String pattern,final Locale locale,final Map registry){
        super("");
        this.setLocale(locale);
        this.registry=registry;
        this.applyPattern(pattern);
    }
    public String toPattern(){
        return this.toPattern;
    }
    public final void applyPattern(final String pattern){
        if(this.registry==null){
            super.applyPattern(pattern);
            this.toPattern=super.toPattern();
            return;
        }
        final ArrayList foundFormats=new ArrayList();
        final ArrayList foundDescriptions=new ArrayList();
        final StringBuffer stripCustom=new StringBuffer(pattern.length());
        final ParsePosition pos=new ParsePosition(0);
        final char[] c=pattern.toCharArray();
        int fmtCount=0;
        while(pos.getIndex()<pattern.length()){
            switch(c[pos.getIndex()]){
                case '\'':{
                    this.appendQuotedString(pattern,pos,stripCustom,true);
                    continue;
                }
                case '{':{
                    ++fmtCount;
                    this.seekNonWs(pattern,pos);
                    final int start=pos.getIndex();
                    final int index=this.readArgumentIndex(pattern,this.next(pos));
                    stripCustom.append('{').append(index);
                    this.seekNonWs(pattern,pos);
                    Format format=null;
                    String formatDescription=null;
                    if(c[pos.getIndex()]==','){
                        formatDescription=this.parseFormatDescription(pattern,this.next(pos));
                        format=this.getFormat(formatDescription);
                        if(format==null){
                            stripCustom.append(',').append(formatDescription);
                        }
                    }
                    foundFormats.add(format);
                    foundDescriptions.add((format==null)?null:formatDescription);
                    Validate.isTrue(foundFormats.size()==fmtCount);
                    Validate.isTrue(foundDescriptions.size()==fmtCount);
                    if(c[pos.getIndex()]!='}'){
                        throw new IllegalArgumentException("Unreadable format element at position "+start);
                    }
                    break;
                }
            }
            stripCustom.append(c[pos.getIndex()]);
            this.next(pos);
        }
        super.applyPattern(stripCustom.toString());
        this.toPattern=this.insertFormats(super.toPattern(),foundDescriptions);
        if(this.containsElements(foundFormats)){
            final Format[] origFormats=this.getFormats();
            int i=0;
            for(final Format f : foundFormats){
                if(f!=null){
                    origFormats[i]=f;
                }
                ++i;
            }
            super.setFormats(origFormats);
        }
    }
    public void setFormat(final int formatElementIndex,final Format newFormat){
        throw new UnsupportedOperationException();
    }
    public void setFormatByArgumentIndex(final int argumentIndex,final Format newFormat){
        throw new UnsupportedOperationException();
    }
    public void setFormats(final Format[] newFormats){
        throw new UnsupportedOperationException();
    }
    public void setFormatsByArgumentIndex(final Format[] newFormats){
        throw new UnsupportedOperationException();
    }
    private Format getFormat(final String desc){
        if(this.registry!=null){
            String name=desc;
            String args=null;
            final int i=desc.indexOf(44);
            if(i>0){
                name=desc.substring(0,i).trim();
                args=desc.substring(i+1).trim();
            }
            final FormatFactory factory=this.registry.get(name);
            if(factory!=null){
                return factory.getFormat(name,args,this.getLocale());
            }
        }
        return null;
    }
    private int readArgumentIndex(final String pattern,final ParsePosition pos){
        final int start=pos.getIndex();
        this.seekNonWs(pattern,pos);
        final StringBuffer result=new StringBuffer();
        boolean error=false;
        while(!error&&pos.getIndex()<pattern.length()){
            char c=pattern.charAt(pos.getIndex());
            Label_0149:{
                if(Character.isWhitespace(c)){
                    this.seekNonWs(pattern,pos);
                    c=pattern.charAt(pos.getIndex());
                    if(c!=','&&c!='}'){
                        error=true;
                        break Label_0149;
                    }
                }
                if((c==','||c=='}')&&result.length()>0){
                    try{
                        return Integer.parseInt(result.toString());
                    }
                    catch(NumberFormatException ex){
                    }
                }
                error=!Character.isDigit(c);
                result.append(c);
            }
            this.next(pos);
        }
        if(error){
            throw new IllegalArgumentException("Invalid format argument index at position "+start+": "+pattern.substring(start,pos.getIndex()));
        }
        throw new IllegalArgumentException("Unterminated format element at position "+start);
    }
    private String parseFormatDescription(final String pattern,final ParsePosition pos){
        final int start=pos.getIndex();
        this.seekNonWs(pattern,pos);
        final int text=pos.getIndex();
        int depth=1;
        while(pos.getIndex()<pattern.length()){
            switch(pattern.charAt(pos.getIndex())){
                case '{':{
                    ++depth;
                    break;
                }
                case '}':{
                    if(--depth==0){
                        return pattern.substring(text,pos.getIndex());
                    }
                    break;
                }
                case '\'':{
                    this.getQuotedString(pattern,pos,false);
                    break;
                }
            }
            this.next(pos);
        }
        throw new IllegalArgumentException("Unterminated format element at position "+start);
    }
    private String insertFormats(final String pattern,final ArrayList customPatterns){
        if(!this.containsElements(customPatterns)){
            return pattern;
        }
        final StringBuffer sb=new StringBuffer(pattern.length()*2);
        final ParsePosition pos=new ParsePosition(0);
        int fe=-1;
        int depth=0;
        while(pos.getIndex()<pattern.length()){
            final char c=pattern.charAt(pos.getIndex());
            switch(c){
                case '\'':{
                    this.appendQuotedString(pattern,pos,sb,false);
                    continue;
                }
                case '{':{
                    if(++depth==1){
                        ++fe;
                        sb.append('{').append(this.readArgumentIndex(pattern,this.next(pos)));
                        final String customPattern=customPatterns.get(fe);
                        if(customPattern==null){
                            continue;
                        }
                        sb.append(',').append(customPattern);
                        continue;
                    }
                    continue;
                }
                case '}':{
                    --depth;
                    break;
                }
            }
            sb.append(c);
            this.next(pos);
        }
        return sb.toString();
    }
    private void seekNonWs(final String pattern,final ParsePosition pos){
        int len=0;
        final char[] buffer=pattern.toCharArray();
        do{
            len=StrMatcher.splitMatcher().isMatch(buffer,pos.getIndex());
            pos.setIndex(pos.getIndex()+len);
        } while(len>0&&pos.getIndex()<pattern.length());
    }
    private ParsePosition next(final ParsePosition pos){
        pos.setIndex(pos.getIndex()+1);
        return pos;
    }
    private StringBuffer appendQuotedString(final String pattern,final ParsePosition pos,final StringBuffer appendTo,final boolean escapingOn){
        final int start=pos.getIndex();
        final char[] c=pattern.toCharArray();
        if(escapingOn&&c[start]=='\''){
            this.next(pos);
            return (appendTo==null)?null:appendTo.append('\'');
        }
        int lastHold=start;
        for(int i=pos.getIndex();i<pattern.length();++i){
            if(escapingOn&&pattern.substring(i).startsWith("''")){
                appendTo.append(c,lastHold,pos.getIndex()-lastHold).append('\'');
                pos.setIndex(i+"''".length());
                lastHold=pos.getIndex();
            }
            else{
                switch(c[pos.getIndex()]){
                    case '\'':{
                        this.next(pos);
                        return (appendTo==null)?null:appendTo.append(c,lastHold,pos.getIndex()-lastHold);
                    }
                    default:{
                        this.next(pos);
                        break;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Unterminated quoted string at position "+start);
    }
    private void getQuotedString(final String pattern,final ParsePosition pos,final boolean escapingOn){
        this.appendQuotedString(pattern,pos,null,escapingOn);
    }
    private boolean containsElements(final Collection coll){
        if(coll==null||coll.size()==0){
            return false;
        }
        final Iterator iter=coll.iterator();
        while(iter.hasNext()){
            if(iter.next()!=null){
                return true;
            }
        }
        return false;
    }
}
