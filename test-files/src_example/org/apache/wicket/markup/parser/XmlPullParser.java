package org.apache.wicket.markup.parser;

import java.text.*;
import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.io.*;
import java.io.*;
import org.apache.wicket.util.parse.metapattern.parsers.*;
import org.apache.wicket.util.string.*;

public final class XmlPullParser implements IXmlPullParser{
    public static final String STYLE="style";
    public static final String SCRIPT="script";
    private XmlReader xmlReader;
    private FullyBufferedReader input;
    private String skipUntilText;
    private CharSequence lastText;
    private CharSequence doctype;
    private HttpTagType lastType;
    private XmlTag lastTag;
    public XmlPullParser(){
        super();
        this.lastType=HttpTagType.NOT_INITIALIZED;
    }
    public final String getEncoding(){
        return this.xmlReader.getEncoding();
    }
    public final CharSequence getDoctype(){
        return this.doctype;
    }
    public final CharSequence getInputFromPositionMarker(final int toPos){
        return this.input.getSubstring(toPos);
    }
    public final CharSequence getInput(final int fromPos,final int toPos){
        return this.input.getSubstring(fromPos,toPos);
    }
    private final void skipUntil() throws ParseException{
        final int startIndex=this.input.getPosition();
        final int tagNameLen=this.skipUntilText.length();
        int pos=this.input.getPosition()-1;
        String endTagText;
        int lastPos;
        for(endTagText=null,lastPos=0;!this.skipUntilText.equalsIgnoreCase(endTagText);endTagText=this.input.getSubstring(lastPos,lastPos+tagNameLen).toString()){
            pos=this.input.find("</",pos+1);
            if(pos==-1||pos+(tagNameLen+2)>=this.input.size()){
                throw new ParseException(this.skipUntilText+" tag not closed"+this.getLineAndColumnText(),startIndex);
            }
            lastPos=pos+2;
        }
        this.input.setPosition(pos);
        this.lastText=this.input.getSubstring(startIndex,pos);
        this.lastType=HttpTagType.BODY;
        lastPos=this.input.find('>',lastPos+tagNameLen);
        if(lastPos==-1){
            throw new ParseException(this.skipUntilText+" tag not closed"+this.getLineAndColumnText(),startIndex);
        }
        this.skipUntilText=null;
    }
    private String getLineAndColumnText(){
        return " (line "+this.input.getLineNumber()+", column "+this.input.getColumnNumber()+")";
    }
    public final HttpTagType next() throws ParseException{
        if(this.input.getPosition()>=this.input.size()){
            return HttpTagType.NOT_INITIALIZED;
        }
        if(this.skipUntilText!=null){
            this.skipUntil();
            return this.lastType;
        }
        final int openBracketIndex=this.input.find('<');
        if(this.input.charAt(this.input.getPosition())!='<'){
            if(openBracketIndex==-1){
                this.lastText=this.input.getSubstring(-1);
                this.input.setPosition(this.input.size());
                return this.lastType=HttpTagType.BODY;
            }
            this.lastText=this.input.getSubstring(openBracketIndex);
            this.input.setPosition(openBracketIndex);
            return this.lastType=HttpTagType.BODY;
        }
        else{
            this.input.countLinesTo(openBracketIndex);
            int closeBracketIndex=-1;
            if(openBracketIndex!=-1&&openBracketIndex<this.input.size()-1){
                final char nextChar=this.input.charAt(openBracketIndex+1);
                if(nextChar=='!'||nextChar=='?'){
                    closeBracketIndex=this.input.find('>',openBracketIndex);
                }
                else{
                    closeBracketIndex=this.input.findOutOfQuotes('>',openBracketIndex);
                }
            }
            if(closeBracketIndex==-1){
                throw new ParseException("No matching close bracket at"+this.getLineAndColumnText(),this.input.getPosition());
            }
            this.lastText=this.input.getSubstring(openBracketIndex,closeBracketIndex+1);
            String tagText=this.lastText.subSequence(1,this.lastText.length()-1).toString();
            if(tagText.length()==0){
                throw new ParseException("Found empty tag: '<>' at"+this.getLineAndColumnText(),this.input.getPosition());
            }
            XmlTag.TagType type;
            if(tagText.endsWith("/")){
                type=XmlTag.TagType.OPEN_CLOSE;
                tagText=tagText.substring(0,tagText.length()-1);
            }
            else if(tagText.startsWith("/")){
                type=XmlTag.TagType.CLOSE;
                tagText=tagText.substring(1);
            }
            else{
                type=XmlTag.TagType.OPEN;
                if(tagText.length()>"style".length()&&(tagText.charAt(0)=='s'||tagText.charAt(0)=='S')){
                    final String lowerCase=tagText.substring(0,6).toLowerCase();
                    if(lowerCase.startsWith("script")){
                        this.skipUntilText="script";
                    }
                    else if(lowerCase.startsWith("style")){
                        this.skipUntilText="style";
                    }
                }
            }
            final char firstChar=tagText.charAt(0);
            if(firstChar=='!'||firstChar=='?'){
                this.specialTagHandling(tagText,openBracketIndex,closeBracketIndex);
                this.input.countLinesTo(openBracketIndex);
                final XmlTag.TextSegment text=new XmlTag.TextSegment(this.lastText,openBracketIndex,this.input.getLineNumber(),this.input.getColumnNumber());
                this.lastTag=new XmlTag(text,type);
                return this.lastType;
            }
            final XmlTag.TextSegment text=new XmlTag.TextSegment(this.lastText,openBracketIndex,this.input.getLineNumber(),this.input.getColumnNumber());
            final XmlTag tag=new XmlTag(text,type);
            this.lastTag=tag;
            if(this.parseTagText(tag,tagText)){
                this.input.setPosition(closeBracketIndex+1);
                return this.lastType=HttpTagType.TAG;
            }
            throw new ParseException("Malformed tag"+this.getLineAndColumnText(),openBracketIndex);
        }
    }
    protected void specialTagHandling(String tagText,final int openBracketIndex,int closeBracketIndex) throws ParseException{
        if(tagText.startsWith("!--")){
            if(tagText.contains((CharSequence)"![endif]--")){
                this.lastType=HttpTagType.CONDITIONAL_COMMENT_ENDIF;
                this.input.setPosition(closeBracketIndex+1);
                return;
            }
            if(tagText.startsWith("!--[if ")&&tagText.endsWith("]")){
                int pos=this.input.find("]-->",openBracketIndex+1);
                if(pos==-1){
                    throw new ParseException("Unclosed conditional comment beginning at"+this.getLineAndColumnText(),openBracketIndex);
                }
                pos+=4;
                this.lastText=this.input.getSubstring(openBracketIndex,pos);
                this.input.setPosition(closeBracketIndex+1);
                this.lastType=HttpTagType.CONDITIONAL_COMMENT;
            }
            else{
                int pos=this.input.find("-->",openBracketIndex+1);
                if(pos==-1){
                    throw new ParseException("Unclosed comment beginning at"+this.getLineAndColumnText(),openBracketIndex);
                }
                pos+=3;
                this.lastText=this.input.getSubstring(openBracketIndex,pos);
                this.lastType=HttpTagType.COMMENT;
                this.input.setPosition(pos);
            }
        }
        else{
            if(tagText.equals("![endif]--")){
                this.lastType=HttpTagType.CONDITIONAL_COMMENT_ENDIF;
                this.input.setPosition(closeBracketIndex+1);
                return;
            }
            if(tagText.startsWith("![")){
                final String startText=(tagText.length()<=8)?tagText:tagText.substring(0,8);
                if(startText.toUpperCase().equals("![CDATA[")){
                    int pos2=openBracketIndex;
                    do{
                        closeBracketIndex=this.findChar('>',pos2);
                        if(closeBracketIndex==-1){
                            throw new ParseException("No matching close bracket at"+this.getLineAndColumnText(),this.input.getPosition());
                        }
                        tagText=this.input.getSubstring(openBracketIndex+1,closeBracketIndex).toString();
                        pos2=closeBracketIndex+1;
                    } while(!tagText.endsWith("]]"));
                    this.input.setPosition(closeBracketIndex+1);
                    this.lastText=(CharSequence)tagText;
                    this.lastType=HttpTagType.CDATA;
                    return;
                }
            }
            if(tagText.charAt(0)=='?'){
                this.lastType=HttpTagType.PROCESSING_INSTRUCTION;
                this.input.setPosition(closeBracketIndex+1);
                return;
            }
            if(tagText.startsWith("!DOCTYPE")){
                this.lastType=HttpTagType.DOCTYPE;
                this.doctype=this.input.getSubstring(openBracketIndex+1,closeBracketIndex);
                this.input.setPosition(closeBracketIndex+1);
                return;
            }
            this.lastType=HttpTagType.SPECIAL_TAG;
            this.input.setPosition(closeBracketIndex+1);
        }
    }
    public final XmlTag getElement(){
        return this.lastTag;
    }
    public final CharSequence getString(){
        return this.lastText;
    }
    public final XmlTag nextTag() throws ParseException{
        while(this.next()!=HttpTagType.NOT_INITIALIZED){
            switch(this.lastType){
                case TAG:{
                    return this.lastTag;
                }
                case BODY:{
                }
                case COMMENT:{
                }
                case CONDITIONAL_COMMENT:{
                }
                case CDATA:{
                }
                case PROCESSING_INSTRUCTION:{
                    continue;
                }
            }
        }
        return null;
    }
    private int findChar(final char ch,int startIndex){
        char quote='\0';
        while(startIndex<this.input.size()){
            final char charAt=this.input.charAt(startIndex);
            if(quote!='\0'){
                if(quote==charAt){
                    quote='\0';
                }
            }
            else if(charAt=='\"'||charAt=='\''){
                quote=charAt;
            }
            else if(charAt==ch){
                return startIndex;
            }
            ++startIndex;
        }
        return -1;
    }
    public void parse(final CharSequence string) throws IOException,ResourceStreamNotFoundException{
        this.parse(new ByteArrayInputStream(string.toString().getBytes()),null);
    }
    public void parse(final InputStream in) throws IOException,ResourceStreamNotFoundException{
        this.parse(in,"UTF-8");
    }
    public void parse(final InputStream inputStream,final String encoding) throws IOException{
        Args.notNull((Object)inputStream,"inputStream");
        try{
            this.xmlReader=new XmlReader((InputStream)new BufferedInputStream(inputStream,4000),encoding);
            this.input=new FullyBufferedReader((Reader)this.xmlReader);
        }
        finally{
            IOUtils.closeQuietly((Closeable)inputStream);
            IOUtils.closeQuietly((Closeable)this.xmlReader);
        }
    }
    public final void setPositionMarker(){
        this.input.setPositionMarker(this.input.getPosition());
    }
    public final void setPositionMarker(final int pos){
        this.input.setPositionMarker(pos);
    }
    public String toString(){
        return this.input.toString();
    }
    private boolean parseTagText(final XmlTag tag,final String tagText) throws ParseException{
        final int tagTextLength=tagText.length();
        final TagNameParser tagnameParser=new TagNameParser((CharSequence)tagText);
        if(!tagnameParser.matcher().lookingAt()){
            return false;
        }
        tag.name=tagnameParser.getName();
        tag.namespace=tagnameParser.getNamespace();
        int pos=tagnameParser.matcher().end(0);
        if(pos==tagTextLength){
            return true;
        }
        final VariableAssignmentParser attributeParser=new VariableAssignmentParser((CharSequence)tagText);
        while(attributeParser.matcher().find(pos)){
            String value=attributeParser.getValue();
            if(value==null){
                value="";
            }
            pos=attributeParser.matcher().end(0);
            if(value.startsWith("\"")||value.startsWith("'")){
                value=value.substring(1,value.length()-1);
            }
            value=value.trim();
            value=Strings.unescapeMarkup(value).toString();
            final String key=attributeParser.getKey();
            if(null!=tag.getAttributes().put((Object)key,(Object)value)){
                throw new ParseException("Same attribute found twice: "+key+this.getLineAndColumnText(),this.input.getPosition());
            }
            if(pos==tagTextLength){
                return true;
            }
        }
        return true;
    }
}
