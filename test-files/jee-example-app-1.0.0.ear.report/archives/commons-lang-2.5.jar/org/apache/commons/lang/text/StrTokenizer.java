package org.apache.commons.lang.text;

import org.apache.commons.lang.text.StrBuilder;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.text.StrMatcher;
import java.util.ListIterator;

public class StrTokenizer implements ListIterator,Cloneable{
    private static final StrTokenizer CSV_TOKENIZER_PROTOTYPE;
    private static final StrTokenizer TSV_TOKENIZER_PROTOTYPE;
    private char[] chars;
    private String[] tokens;
    private int tokenPos;
    private StrMatcher delimMatcher;
    private StrMatcher quoteMatcher;
    private StrMatcher ignoredMatcher;
    private StrMatcher trimmerMatcher;
    private boolean emptyAsNull;
    private boolean ignoreEmptyTokens;
    private static StrTokenizer getCSVClone(){
        return (StrTokenizer)StrTokenizer.CSV_TOKENIZER_PROTOTYPE.clone();
    }
    public static StrTokenizer getCSVInstance(){
        return getCSVClone();
    }
    public static StrTokenizer getCSVInstance(final String input){
        final StrTokenizer tok=getCSVClone();
        tok.reset(input);
        return tok;
    }
    public static StrTokenizer getCSVInstance(final char[] input){
        final StrTokenizer tok=getCSVClone();
        tok.reset(input);
        return tok;
    }
    private static StrTokenizer getTSVClone(){
        return (StrTokenizer)StrTokenizer.TSV_TOKENIZER_PROTOTYPE.clone();
    }
    public static StrTokenizer getTSVInstance(){
        return getTSVClone();
    }
    public static StrTokenizer getTSVInstance(final String input){
        final StrTokenizer tok=getTSVClone();
        tok.reset(input);
        return tok;
    }
    public static StrTokenizer getTSVInstance(final char[] input){
        final StrTokenizer tok=getTSVClone();
        tok.reset(input);
        return tok;
    }
    public StrTokenizer(){
        super();
        this.delimMatcher=StrMatcher.splitMatcher();
        this.quoteMatcher=StrMatcher.noneMatcher();
        this.ignoredMatcher=StrMatcher.noneMatcher();
        this.trimmerMatcher=StrMatcher.noneMatcher();
        this.emptyAsNull=false;
        this.ignoreEmptyTokens=true;
        this.chars=null;
    }
    public StrTokenizer(final String input){
        super();
        this.delimMatcher=StrMatcher.splitMatcher();
        this.quoteMatcher=StrMatcher.noneMatcher();
        this.ignoredMatcher=StrMatcher.noneMatcher();
        this.trimmerMatcher=StrMatcher.noneMatcher();
        this.emptyAsNull=false;
        this.ignoreEmptyTokens=true;
        if(input!=null){
            this.chars=input.toCharArray();
        }
        else{
            this.chars=null;
        }
    }
    public StrTokenizer(final String input,final char delim){
        this(input);
        this.setDelimiterChar(delim);
    }
    public StrTokenizer(final String input,final String delim){
        this(input);
        this.setDelimiterString(delim);
    }
    public StrTokenizer(final String input,final StrMatcher delim){
        this(input);
        this.setDelimiterMatcher(delim);
    }
    public StrTokenizer(final String input,final char delim,final char quote){
        this(input,delim);
        this.setQuoteChar(quote);
    }
    public StrTokenizer(final String input,final StrMatcher delim,final StrMatcher quote){
        this(input,delim);
        this.setQuoteMatcher(quote);
    }
    public StrTokenizer(final char[] input){
        super();
        this.delimMatcher=StrMatcher.splitMatcher();
        this.quoteMatcher=StrMatcher.noneMatcher();
        this.ignoredMatcher=StrMatcher.noneMatcher();
        this.trimmerMatcher=StrMatcher.noneMatcher();
        this.emptyAsNull=false;
        this.ignoreEmptyTokens=true;
        this.chars=input;
    }
    public StrTokenizer(final char[] input,final char delim){
        this(input);
        this.setDelimiterChar(delim);
    }
    public StrTokenizer(final char[] input,final String delim){
        this(input);
        this.setDelimiterString(delim);
    }
    public StrTokenizer(final char[] input,final StrMatcher delim){
        this(input);
        this.setDelimiterMatcher(delim);
    }
    public StrTokenizer(final char[] input,final char delim,final char quote){
        this(input,delim);
        this.setQuoteChar(quote);
    }
    public StrTokenizer(final char[] input,final StrMatcher delim,final StrMatcher quote){
        this(input,delim);
        this.setQuoteMatcher(quote);
    }
    public int size(){
        this.checkTokenized();
        return this.tokens.length;
    }
    public String nextToken(){
        if(this.hasNext()){
            return this.tokens[this.tokenPos++];
        }
        return null;
    }
    public String previousToken(){
        if(this.hasPrevious()){
            final String[] tokens=this.tokens;
            final int tokenPos=this.tokenPos-1;
            this.tokenPos=tokenPos;
            return tokens[tokenPos];
        }
        return null;
    }
    public String[] getTokenArray(){
        this.checkTokenized();
        return this.tokens.clone();
    }
    public List getTokenList(){
        this.checkTokenized();
        final List list=new ArrayList(this.tokens.length);
        for(int i=0;i<this.tokens.length;++i){
            list.add(this.tokens[i]);
        }
        return list;
    }
    public StrTokenizer reset(){
        this.tokenPos=0;
        this.tokens=null;
        return this;
    }
    public StrTokenizer reset(final String input){
        this.reset();
        if(input!=null){
            this.chars=input.toCharArray();
        }
        else{
            this.chars=null;
        }
        return this;
    }
    public StrTokenizer reset(final char[] input){
        this.reset();
        this.chars=input;
        return this;
    }
    public boolean hasNext(){
        this.checkTokenized();
        return this.tokenPos<this.tokens.length;
    }
    public Object next(){
        if(this.hasNext()){
            return this.tokens[this.tokenPos++];
        }
        throw new NoSuchElementException();
    }
    public int nextIndex(){
        return this.tokenPos;
    }
    public boolean hasPrevious(){
        this.checkTokenized();
        return this.tokenPos>0;
    }
    public Object previous(){
        if(this.hasPrevious()){
            final String[] tokens=this.tokens;
            final int tokenPos=this.tokenPos-1;
            this.tokenPos=tokenPos;
            return tokens[tokenPos];
        }
        throw new NoSuchElementException();
    }
    public int previousIndex(){
        return this.tokenPos-1;
    }
    public void remove(){
        throw new UnsupportedOperationException("remove() is unsupported");
    }
    public void set(final Object obj){
        throw new UnsupportedOperationException("set() is unsupported");
    }
    public void add(final Object obj){
        throw new UnsupportedOperationException("add() is unsupported");
    }
    private void checkTokenized(){
        if(this.tokens==null){
            if(this.chars==null){
                final List split=this.tokenize(null,0,0);
                this.tokens=split.toArray(new String[split.size()]);
            }
            else{
                final List split=this.tokenize(this.chars,0,this.chars.length);
                this.tokens=split.toArray(new String[split.size()]);
            }
        }
    }
    protected List tokenize(final char[] chars,final int offset,final int count){
        if(chars==null||count==0){
            return Collections.EMPTY_LIST;
        }
        final StrBuilder buf=new StrBuilder();
        final List tokens=new ArrayList();
        int pos=offset;
        while(pos>=0&&pos<count){
            pos=this.readNextToken(chars,pos,count,buf,tokens);
            if(pos>=count){
                this.addToken(tokens,"");
            }
        }
        return tokens;
    }
    private void addToken(final List list,String tok){
        if(tok==null||tok.length()==0){
            if(this.isIgnoreEmptyTokens()){
                return;
            }
            if(this.isEmptyTokenAsNull()){
                tok=null;
            }
        }
        list.add(tok);
    }
    private int readNextToken(final char[] chars,int start,final int len,final StrBuilder workArea,final List tokens){
        while(start<len){
            final int removeLen=Math.max(this.getIgnoredMatcher().isMatch(chars,start,start,len),this.getTrimmerMatcher().isMatch(chars,start,start,len));
            if(removeLen==0||this.getDelimiterMatcher().isMatch(chars,start,start,len)>0){
                break;
            }
            if(this.getQuoteMatcher().isMatch(chars,start,start,len)>0){
                break;
            }
            start+=removeLen;
        }
        if(start>=len){
            this.addToken(tokens,"");
            return -1;
        }
        final int delimLen=this.getDelimiterMatcher().isMatch(chars,start,start,len);
        if(delimLen>0){
            this.addToken(tokens,"");
            return start+delimLen;
        }
        final int quoteLen=this.getQuoteMatcher().isMatch(chars,start,start,len);
        if(quoteLen>0){
            return this.readWithQuotes(chars,start+quoteLen,len,workArea,tokens,start,quoteLen);
        }
        return this.readWithQuotes(chars,start,len,workArea,tokens,0,0);
    }
    private int readWithQuotes(final char[] chars,final int start,final int len,final StrBuilder workArea,final List tokens,final int quoteStart,final int quoteLen){
        workArea.clear();
        int pos=start;
        boolean quoting=quoteLen>0;
        int trimStart=0;
        while(pos<len){
            if(quoting){
                if(this.isQuote(chars,pos,len,quoteStart,quoteLen)){
                    if(this.isQuote(chars,pos+quoteLen,len,quoteStart,quoteLen)){
                        workArea.append(chars,pos,quoteLen);
                        pos+=quoteLen*2;
                        trimStart=workArea.size();
                    }
                    else{
                        quoting=false;
                        pos+=quoteLen;
                    }
                }
                else{
                    workArea.append(chars[pos++]);
                    trimStart=workArea.size();
                }
            }
            else{
                final int delimLen=this.getDelimiterMatcher().isMatch(chars,pos,start,len);
                if(delimLen>0){
                    this.addToken(tokens,workArea.substring(0,trimStart));
                    return pos+delimLen;
                }
                if(quoteLen>0&&this.isQuote(chars,pos,len,quoteStart,quoteLen)){
                    quoting=true;
                    pos+=quoteLen;
                }
                else{
                    final int ignoredLen=this.getIgnoredMatcher().isMatch(chars,pos,start,len);
                    if(ignoredLen>0){
                        pos+=ignoredLen;
                    }
                    else{
                        final int trimmedLen=this.getTrimmerMatcher().isMatch(chars,pos,start,len);
                        if(trimmedLen>0){
                            workArea.append(chars,pos,trimmedLen);
                            pos+=trimmedLen;
                        }
                        else{
                            workArea.append(chars[pos++]);
                            trimStart=workArea.size();
                        }
                    }
                }
            }
        }
        this.addToken(tokens,workArea.substring(0,trimStart));
        return -1;
    }
    private boolean isQuote(final char[] chars,final int pos,final int len,final int quoteStart,final int quoteLen){
        for(int i=0;i<quoteLen;++i){
            if(pos+i>=len||chars[pos+i]!=chars[quoteStart+i]){
                return false;
            }
        }
        return true;
    }
    public StrMatcher getDelimiterMatcher(){
        return this.delimMatcher;
    }
    public StrTokenizer setDelimiterMatcher(final StrMatcher delim){
        if(delim==null){
            this.delimMatcher=StrMatcher.noneMatcher();
        }
        else{
            this.delimMatcher=delim;
        }
        return this;
    }
    public StrTokenizer setDelimiterChar(final char delim){
        return this.setDelimiterMatcher(StrMatcher.charMatcher(delim));
    }
    public StrTokenizer setDelimiterString(final String delim){
        return this.setDelimiterMatcher(StrMatcher.stringMatcher(delim));
    }
    public StrMatcher getQuoteMatcher(){
        return this.quoteMatcher;
    }
    public StrTokenizer setQuoteMatcher(final StrMatcher quote){
        if(quote!=null){
            this.quoteMatcher=quote;
        }
        return this;
    }
    public StrTokenizer setQuoteChar(final char quote){
        return this.setQuoteMatcher(StrMatcher.charMatcher(quote));
    }
    public StrMatcher getIgnoredMatcher(){
        return this.ignoredMatcher;
    }
    public StrTokenizer setIgnoredMatcher(final StrMatcher ignored){
        if(ignored!=null){
            this.ignoredMatcher=ignored;
        }
        return this;
    }
    public StrTokenizer setIgnoredChar(final char ignored){
        return this.setIgnoredMatcher(StrMatcher.charMatcher(ignored));
    }
    public StrMatcher getTrimmerMatcher(){
        return this.trimmerMatcher;
    }
    public StrTokenizer setTrimmerMatcher(final StrMatcher trimmer){
        if(trimmer!=null){
            this.trimmerMatcher=trimmer;
        }
        return this;
    }
    public boolean isEmptyTokenAsNull(){
        return this.emptyAsNull;
    }
    public StrTokenizer setEmptyTokenAsNull(final boolean emptyAsNull){
        this.emptyAsNull=emptyAsNull;
        return this;
    }
    public boolean isIgnoreEmptyTokens(){
        return this.ignoreEmptyTokens;
    }
    public StrTokenizer setIgnoreEmptyTokens(final boolean ignoreEmptyTokens){
        this.ignoreEmptyTokens=ignoreEmptyTokens;
        return this;
    }
    public String getContent(){
        if(this.chars==null){
            return null;
        }
        return new String(this.chars);
    }
    public Object clone(){
        try{
            return this.cloneReset();
        }
        catch(CloneNotSupportedException ex){
            return null;
        }
    }
    Object cloneReset() throws CloneNotSupportedException{
        final StrTokenizer cloned=(StrTokenizer)super.clone();
        if(cloned.chars!=null){
            cloned.chars=cloned.chars.clone();
        }
        cloned.reset();
        return cloned;
    }
    public String toString(){
        if(this.tokens==null){
            return "StrTokenizer[not tokenized yet]";
        }
        return "StrTokenizer"+this.getTokenList();
    }
    static{
        (CSV_TOKENIZER_PROTOTYPE=new StrTokenizer()).setDelimiterMatcher(StrMatcher.commaMatcher());
        StrTokenizer.CSV_TOKENIZER_PROTOTYPE.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        StrTokenizer.CSV_TOKENIZER_PROTOTYPE.setIgnoredMatcher(StrMatcher.noneMatcher());
        StrTokenizer.CSV_TOKENIZER_PROTOTYPE.setTrimmerMatcher(StrMatcher.trimMatcher());
        StrTokenizer.CSV_TOKENIZER_PROTOTYPE.setEmptyTokenAsNull(false);
        StrTokenizer.CSV_TOKENIZER_PROTOTYPE.setIgnoreEmptyTokens(false);
        (TSV_TOKENIZER_PROTOTYPE=new StrTokenizer()).setDelimiterMatcher(StrMatcher.tabMatcher());
        StrTokenizer.TSV_TOKENIZER_PROTOTYPE.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        StrTokenizer.TSV_TOKENIZER_PROTOTYPE.setIgnoredMatcher(StrMatcher.noneMatcher());
        StrTokenizer.TSV_TOKENIZER_PROTOTYPE.setTrimmerMatcher(StrMatcher.trimMatcher());
        StrTokenizer.TSV_TOKENIZER_PROTOTYPE.setEmptyTokenAsNull(false);
        StrTokenizer.TSV_TOKENIZER_PROTOTYPE.setIgnoreEmptyTokens(false);
    }
}
