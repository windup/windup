package org.apache.commons.lang.text;

import java.util.List;
import java.io.Writer;
import java.io.Reader;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.lang.text.StrMatcher;
import java.util.Iterator;
import java.util.Collection;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.ArrayUtils;

public class StrBuilder implements Cloneable{
    static final int CAPACITY=32;
    private static final long serialVersionUID=7628716375283629643L;
    protected char[] buffer;
    protected int size;
    private String newLine;
    private String nullText;
    public StrBuilder(){
        this(32);
    }
    public StrBuilder(int initialCapacity){
        super();
        if(initialCapacity<=0){
            initialCapacity=32;
        }
        this.buffer=new char[initialCapacity];
    }
    public StrBuilder(final String str){
        super();
        if(str==null){
            this.buffer=new char[32];
        }
        else{
            this.buffer=new char[str.length()+32];
            this.append(str);
        }
    }
    public String getNewLineText(){
        return this.newLine;
    }
    public StrBuilder setNewLineText(final String newLine){
        this.newLine=newLine;
        return this;
    }
    public String getNullText(){
        return this.nullText;
    }
    public StrBuilder setNullText(String nullText){
        if(nullText!=null&&nullText.length()==0){
            nullText=null;
        }
        this.nullText=nullText;
        return this;
    }
    public int length(){
        return this.size;
    }
    public StrBuilder setLength(final int length){
        if(length<0){
            throw new StringIndexOutOfBoundsException(length);
        }
        if(length<this.size){
            this.size=length;
        }
        else if(length>this.size){
            this.ensureCapacity(length);
            final int oldEnd=this.size;
            this.size=length;
            for(int i=oldEnd;i<length;++i){
                this.buffer[i]='\0';
            }
        }
        return this;
    }
    public int capacity(){
        return this.buffer.length;
    }
    public StrBuilder ensureCapacity(final int capacity){
        if(capacity>this.buffer.length){
            final char[] old=this.buffer;
            System.arraycopy(old,0,this.buffer=new char[capacity*2],0,this.size);
        }
        return this;
    }
    public StrBuilder minimizeCapacity(){
        if(this.buffer.length>this.length()){
            final char[] old=this.buffer;
            System.arraycopy(old,0,this.buffer=new char[this.length()],0,this.size);
        }
        return this;
    }
    public int size(){
        return this.size;
    }
    public boolean isEmpty(){
        return this.size==0;
    }
    public StrBuilder clear(){
        this.size=0;
        return this;
    }
    public char charAt(final int index){
        if(index<0||index>=this.length()){
            throw new StringIndexOutOfBoundsException(index);
        }
        return this.buffer[index];
    }
    public StrBuilder setCharAt(final int index,final char ch){
        if(index<0||index>=this.length()){
            throw new StringIndexOutOfBoundsException(index);
        }
        this.buffer[index]=ch;
        return this;
    }
    public StrBuilder deleteCharAt(final int index){
        if(index<0||index>=this.size){
            throw new StringIndexOutOfBoundsException(index);
        }
        this.deleteImpl(index,index+1,1);
        return this;
    }
    public char[] toCharArray(){
        if(this.size==0){
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        final char[] chars=new char[this.size];
        System.arraycopy(this.buffer,0,chars,0,this.size);
        return chars;
    }
    public char[] toCharArray(final int startIndex,int endIndex){
        endIndex=this.validateRange(startIndex,endIndex);
        final int len=endIndex-startIndex;
        if(len==0){
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        }
        final char[] chars=new char[len];
        System.arraycopy(this.buffer,startIndex,chars,0,len);
        return chars;
    }
    public char[] getChars(char[] destination){
        final int len=this.length();
        if(destination==null||destination.length<len){
            destination=new char[len];
        }
        System.arraycopy(this.buffer,0,destination,0,len);
        return destination;
    }
    public void getChars(final int startIndex,final int endIndex,final char[] destination,final int destinationIndex){
        if(startIndex<0){
            throw new StringIndexOutOfBoundsException(startIndex);
        }
        if(endIndex<0||endIndex>this.length()){
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if(startIndex>endIndex){
            throw new StringIndexOutOfBoundsException("end < start");
        }
        System.arraycopy(this.buffer,startIndex,destination,destinationIndex,endIndex-startIndex);
    }
    public StrBuilder appendNewLine(){
        if(this.newLine==null){
            this.append(SystemUtils.LINE_SEPARATOR);
            return this;
        }
        return this.append(this.newLine);
    }
    public StrBuilder appendNull(){
        if(this.nullText==null){
            return this;
        }
        return this.append(this.nullText);
    }
    public StrBuilder append(final Object obj){
        if(obj==null){
            return this.appendNull();
        }
        return this.append(obj.toString());
    }
    public StrBuilder append(final String str){
        if(str==null){
            return this.appendNull();
        }
        final int strLen=str.length();
        if(strLen>0){
            final int len=this.length();
            this.ensureCapacity(len+strLen);
            str.getChars(0,strLen,this.buffer,len);
            this.size+=strLen;
        }
        return this;
    }
    public StrBuilder append(final String str,final int startIndex,final int length){
        if(str==null){
            return this.appendNull();
        }
        if(startIndex<0||startIndex>str.length()){
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if(length<0||startIndex+length>str.length()){
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if(length>0){
            final int len=this.length();
            this.ensureCapacity(len+length);
            str.getChars(startIndex,startIndex+length,this.buffer,len);
            this.size+=length;
        }
        return this;
    }
    public StrBuilder append(final StringBuffer str){
        if(str==null){
            return this.appendNull();
        }
        final int strLen=str.length();
        if(strLen>0){
            final int len=this.length();
            this.ensureCapacity(len+strLen);
            str.getChars(0,strLen,this.buffer,len);
            this.size+=strLen;
        }
        return this;
    }
    public StrBuilder append(final StringBuffer str,final int startIndex,final int length){
        if(str==null){
            return this.appendNull();
        }
        if(startIndex<0||startIndex>str.length()){
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if(length<0||startIndex+length>str.length()){
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if(length>0){
            final int len=this.length();
            this.ensureCapacity(len+length);
            str.getChars(startIndex,startIndex+length,this.buffer,len);
            this.size+=length;
        }
        return this;
    }
    public StrBuilder append(final StrBuilder str){
        if(str==null){
            return this.appendNull();
        }
        final int strLen=str.length();
        if(strLen>0){
            final int len=this.length();
            this.ensureCapacity(len+strLen);
            System.arraycopy(str.buffer,0,this.buffer,len,strLen);
            this.size+=strLen;
        }
        return this;
    }
    public StrBuilder append(final StrBuilder str,final int startIndex,final int length){
        if(str==null){
            return this.appendNull();
        }
        if(startIndex<0||startIndex>str.length()){
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
        }
        if(length<0||startIndex+length>str.length()){
            throw new StringIndexOutOfBoundsException("length must be valid");
        }
        if(length>0){
            final int len=this.length();
            this.ensureCapacity(len+length);
            str.getChars(startIndex,startIndex+length,this.buffer,len);
            this.size+=length;
        }
        return this;
    }
    public StrBuilder append(final char[] chars){
        if(chars==null){
            return this.appendNull();
        }
        final int strLen=chars.length;
        if(strLen>0){
            final int len=this.length();
            this.ensureCapacity(len+strLen);
            System.arraycopy(chars,0,this.buffer,len,strLen);
            this.size+=strLen;
        }
        return this;
    }
    public StrBuilder append(final char[] chars,final int startIndex,final int length){
        if(chars==null){
            return this.appendNull();
        }
        if(startIndex<0||startIndex>chars.length){
            throw new StringIndexOutOfBoundsException("Invalid startIndex: "+length);
        }
        if(length<0||startIndex+length>chars.length){
            throw new StringIndexOutOfBoundsException("Invalid length: "+length);
        }
        if(length>0){
            final int len=this.length();
            this.ensureCapacity(len+length);
            System.arraycopy(chars,startIndex,this.buffer,len,length);
            this.size+=length;
        }
        return this;
    }
    public StrBuilder append(final boolean value){
        if(value){
            this.ensureCapacity(this.size+4);
            this.buffer[this.size++]='t';
            this.buffer[this.size++]='r';
            this.buffer[this.size++]='u';
            this.buffer[this.size++]='e';
        }
        else{
            this.ensureCapacity(this.size+5);
            this.buffer[this.size++]='f';
            this.buffer[this.size++]='a';
            this.buffer[this.size++]='l';
            this.buffer[this.size++]='s';
            this.buffer[this.size++]='e';
        }
        return this;
    }
    public StrBuilder append(final char ch){
        final int len=this.length();
        this.ensureCapacity(len+1);
        this.buffer[this.size++]=ch;
        return this;
    }
    public StrBuilder append(final int value){
        return this.append(String.valueOf(value));
    }
    public StrBuilder append(final long value){
        return this.append(String.valueOf(value));
    }
    public StrBuilder append(final float value){
        return this.append(String.valueOf(value));
    }
    public StrBuilder append(final double value){
        return this.append(String.valueOf(value));
    }
    public StrBuilder appendln(final Object obj){
        return this.append(obj).appendNewLine();
    }
    public StrBuilder appendln(final String str){
        return this.append(str).appendNewLine();
    }
    public StrBuilder appendln(final String str,final int startIndex,final int length){
        return this.append(str,startIndex,length).appendNewLine();
    }
    public StrBuilder appendln(final StringBuffer str){
        return this.append(str).appendNewLine();
    }
    public StrBuilder appendln(final StringBuffer str,final int startIndex,final int length){
        return this.append(str,startIndex,length).appendNewLine();
    }
    public StrBuilder appendln(final StrBuilder str){
        return this.append(str).appendNewLine();
    }
    public StrBuilder appendln(final StrBuilder str,final int startIndex,final int length){
        return this.append(str,startIndex,length).appendNewLine();
    }
    public StrBuilder appendln(final char[] chars){
        return this.append(chars).appendNewLine();
    }
    public StrBuilder appendln(final char[] chars,final int startIndex,final int length){
        return this.append(chars,startIndex,length).appendNewLine();
    }
    public StrBuilder appendln(final boolean value){
        return this.append(value).appendNewLine();
    }
    public StrBuilder appendln(final char ch){
        return this.append(ch).appendNewLine();
    }
    public StrBuilder appendln(final int value){
        return this.append(value).appendNewLine();
    }
    public StrBuilder appendln(final long value){
        return this.append(value).appendNewLine();
    }
    public StrBuilder appendln(final float value){
        return this.append(value).appendNewLine();
    }
    public StrBuilder appendln(final double value){
        return this.append(value).appendNewLine();
    }
    public StrBuilder appendAll(final Object[] array){
        if(array!=null&&array.length>0){
            for(int i=0;i<array.length;++i){
                this.append(array[i]);
            }
        }
        return this;
    }
    public StrBuilder appendAll(final Collection coll){
        if(coll!=null&&coll.size()>0){
            final Iterator it=coll.iterator();
            while(it.hasNext()){
                this.append(it.next());
            }
        }
        return this;
    }
    public StrBuilder appendAll(final Iterator it){
        if(it!=null){
            while(it.hasNext()){
                this.append(it.next());
            }
        }
        return this;
    }
    public StrBuilder appendWithSeparators(final Object[] array,String separator){
        if(array!=null&&array.length>0){
            separator=((separator==null)?"":separator);
            this.append(array[0]);
            for(int i=1;i<array.length;++i){
                this.append(separator);
                this.append(array[i]);
            }
        }
        return this;
    }
    public StrBuilder appendWithSeparators(final Collection coll,String separator){
        if(coll!=null&&coll.size()>0){
            separator=((separator==null)?"":separator);
            final Iterator it=coll.iterator();
            while(it.hasNext()){
                this.append(it.next());
                if(it.hasNext()){
                    this.append(separator);
                }
            }
        }
        return this;
    }
    public StrBuilder appendWithSeparators(final Iterator it,String separator){
        if(it!=null){
            separator=((separator==null)?"":separator);
            while(it.hasNext()){
                this.append(it.next());
                if(it.hasNext()){
                    this.append(separator);
                }
            }
        }
        return this;
    }
    public StrBuilder appendSeparator(final String separator){
        return this.appendSeparator(separator,null);
    }
    public StrBuilder appendSeparator(final String standard,final String defaultIfEmpty){
        final String str=this.isEmpty()?defaultIfEmpty:standard;
        if(str!=null){
            this.append(str);
        }
        return this;
    }
    public StrBuilder appendSeparator(final char separator){
        if(this.size()>0){
            this.append(separator);
        }
        return this;
    }
    public StrBuilder appendSeparator(final char standard,final char defaultIfEmpty){
        if(this.size()>0){
            this.append(standard);
        }
        else{
            this.append(defaultIfEmpty);
        }
        return this;
    }
    public StrBuilder appendSeparator(final String separator,final int loopIndex){
        if(separator!=null&&loopIndex>0){
            this.append(separator);
        }
        return this;
    }
    public StrBuilder appendSeparator(final char separator,final int loopIndex){
        if(loopIndex>0){
            this.append(separator);
        }
        return this;
    }
    public StrBuilder appendPadding(final int length,final char padChar){
        if(length>=0){
            this.ensureCapacity(this.size+length);
            for(int i=0;i<length;++i){
                this.buffer[this.size++]=padChar;
            }
        }
        return this;
    }
    public StrBuilder appendFixedWidthPadLeft(final Object obj,final int width,final char padChar){
        if(width>0){
            this.ensureCapacity(this.size+width);
            String str=(obj==null)?this.getNullText():obj.toString();
            if(str==null){
                str="";
            }
            final int strLen=str.length();
            if(strLen>=width){
                str.getChars(strLen-width,strLen,this.buffer,this.size);
            }
            else{
                final int padLen=width-strLen;
                for(int i=0;i<padLen;++i){
                    this.buffer[this.size+i]=padChar;
                }
                str.getChars(0,strLen,this.buffer,this.size+padLen);
            }
            this.size+=width;
        }
        return this;
    }
    public StrBuilder appendFixedWidthPadLeft(final int value,final int width,final char padChar){
        return this.appendFixedWidthPadLeft(String.valueOf(value),width,padChar);
    }
    public StrBuilder appendFixedWidthPadRight(final Object obj,final int width,final char padChar){
        if(width>0){
            this.ensureCapacity(this.size+width);
            String str=(obj==null)?this.getNullText():obj.toString();
            if(str==null){
                str="";
            }
            final int strLen=str.length();
            if(strLen>=width){
                str.getChars(0,width,this.buffer,this.size);
            }
            else{
                final int padLen=width-strLen;
                str.getChars(0,strLen,this.buffer,this.size);
                for(int i=0;i<padLen;++i){
                    this.buffer[this.size+strLen+i]=padChar;
                }
            }
            this.size+=width;
        }
        return this;
    }
    public StrBuilder appendFixedWidthPadRight(final int value,final int width,final char padChar){
        return this.appendFixedWidthPadRight(String.valueOf(value),width,padChar);
    }
    public StrBuilder insert(final int index,final Object obj){
        if(obj==null){
            return this.insert(index,this.nullText);
        }
        return this.insert(index,obj.toString());
    }
    public StrBuilder insert(final int index,String str){
        this.validateIndex(index);
        if(str==null){
            str=this.nullText;
        }
        final int strLen=(str==null)?0:str.length();
        if(strLen>0){
            final int newSize=this.size+strLen;
            this.ensureCapacity(newSize);
            System.arraycopy(this.buffer,index,this.buffer,index+strLen,this.size-index);
            this.size=newSize;
            str.getChars(0,strLen,this.buffer,index);
        }
        return this;
    }
    public StrBuilder insert(final int index,final char[] chars){
        this.validateIndex(index);
        if(chars==null){
            return this.insert(index,this.nullText);
        }
        final int len=chars.length;
        if(len>0){
            this.ensureCapacity(this.size+len);
            System.arraycopy(this.buffer,index,this.buffer,index+len,this.size-index);
            System.arraycopy(chars,0,this.buffer,index,len);
            this.size+=len;
        }
        return this;
    }
    public StrBuilder insert(final int index,final char[] chars,final int offset,final int length){
        this.validateIndex(index);
        if(chars==null){
            return this.insert(index,this.nullText);
        }
        if(offset<0||offset>chars.length){
            throw new StringIndexOutOfBoundsException("Invalid offset: "+offset);
        }
        if(length<0||offset+length>chars.length){
            throw new StringIndexOutOfBoundsException("Invalid length: "+length);
        }
        if(length>0){
            this.ensureCapacity(this.size+length);
            System.arraycopy(this.buffer,index,this.buffer,index+length,this.size-index);
            System.arraycopy(chars,offset,this.buffer,index,length);
            this.size+=length;
        }
        return this;
    }
    public StrBuilder insert(int index,final boolean value){
        this.validateIndex(index);
        if(value){
            this.ensureCapacity(this.size+4);
            System.arraycopy(this.buffer,index,this.buffer,index+4,this.size-index);
            this.buffer[index++]='t';
            this.buffer[index++]='r';
            this.buffer[index++]='u';
            this.buffer[index]='e';
            this.size+=4;
        }
        else{
            this.ensureCapacity(this.size+5);
            System.arraycopy(this.buffer,index,this.buffer,index+5,this.size-index);
            this.buffer[index++]='f';
            this.buffer[index++]='a';
            this.buffer[index++]='l';
            this.buffer[index++]='s';
            this.buffer[index]='e';
            this.size+=5;
        }
        return this;
    }
    public StrBuilder insert(final int index,final char value){
        this.validateIndex(index);
        this.ensureCapacity(this.size+1);
        System.arraycopy(this.buffer,index,this.buffer,index+1,this.size-index);
        this.buffer[index]=value;
        ++this.size;
        return this;
    }
    public StrBuilder insert(final int index,final int value){
        return this.insert(index,String.valueOf(value));
    }
    public StrBuilder insert(final int index,final long value){
        return this.insert(index,String.valueOf(value));
    }
    public StrBuilder insert(final int index,final float value){
        return this.insert(index,String.valueOf(value));
    }
    public StrBuilder insert(final int index,final double value){
        return this.insert(index,String.valueOf(value));
    }
    private void deleteImpl(final int startIndex,final int endIndex,final int len){
        System.arraycopy(this.buffer,endIndex,this.buffer,startIndex,this.size-endIndex);
        this.size-=len;
    }
    public StrBuilder delete(final int startIndex,int endIndex){
        endIndex=this.validateRange(startIndex,endIndex);
        final int len=endIndex-startIndex;
        if(len>0){
            this.deleteImpl(startIndex,endIndex,len);
        }
        return this;
    }
    public StrBuilder deleteAll(final char ch){
        for(int i=0;i<this.size;++i){
            if(this.buffer[i]==ch){
                final int start=i;
                while(++i<this.size&&this.buffer[i]==ch){
                }
                final int len=i-start;
                this.deleteImpl(start,i,len);
                i-=len;
            }
        }
        return this;
    }
    public StrBuilder deleteFirst(final char ch){
        for(int i=0;i<this.size;++i){
            if(this.buffer[i]==ch){
                this.deleteImpl(i,i+1,1);
                break;
            }
        }
        return this;
    }
    public StrBuilder deleteAll(final String str){
        final int len=(str==null)?0:str.length();
        if(len>0){
            for(int index=this.indexOf(str,0);index>=0;index=this.indexOf(str,index)){
                this.deleteImpl(index,index+len,len);
            }
        }
        return this;
    }
    public StrBuilder deleteFirst(final String str){
        final int len=(str==null)?0:str.length();
        if(len>0){
            final int index=this.indexOf(str,0);
            if(index>=0){
                this.deleteImpl(index,index+len,len);
            }
        }
        return this;
    }
    public StrBuilder deleteAll(final StrMatcher matcher){
        return this.replace(matcher,null,0,this.size,-1);
    }
    public StrBuilder deleteFirst(final StrMatcher matcher){
        return this.replace(matcher,null,0,this.size,1);
    }
    private void replaceImpl(final int startIndex,final int endIndex,final int removeLen,final String insertStr,final int insertLen){
        final int newSize=this.size-removeLen+insertLen;
        if(insertLen!=removeLen){
            this.ensureCapacity(newSize);
            System.arraycopy(this.buffer,endIndex,this.buffer,startIndex+insertLen,this.size-endIndex);
            this.size=newSize;
        }
        if(insertLen>0){
            insertStr.getChars(0,insertLen,this.buffer,startIndex);
        }
    }
    public StrBuilder replace(final int startIndex,int endIndex,final String replaceStr){
        endIndex=this.validateRange(startIndex,endIndex);
        final int insertLen=(replaceStr==null)?0:replaceStr.length();
        this.replaceImpl(startIndex,endIndex,endIndex-startIndex,replaceStr,insertLen);
        return this;
    }
    public StrBuilder replaceAll(final char search,final char replace){
        if(search!=replace){
            for(int i=0;i<this.size;++i){
                if(this.buffer[i]==search){
                    this.buffer[i]=replace;
                }
            }
        }
        return this;
    }
    public StrBuilder replaceFirst(final char search,final char replace){
        if(search!=replace){
            for(int i=0;i<this.size;++i){
                if(this.buffer[i]==search){
                    this.buffer[i]=replace;
                    break;
                }
            }
        }
        return this;
    }
    public StrBuilder replaceAll(final String searchStr,final String replaceStr){
        final int searchLen=(searchStr==null)?0:searchStr.length();
        if(searchLen>0){
            for(int replaceLen=(replaceStr==null)?0:replaceStr.length(),index=this.indexOf(searchStr,0);index>=0;index=this.indexOf(searchStr,index+replaceLen)){
                this.replaceImpl(index,index+searchLen,searchLen,replaceStr,replaceLen);
            }
        }
        return this;
    }
    public StrBuilder replaceFirst(final String searchStr,final String replaceStr){
        final int searchLen=(searchStr==null)?0:searchStr.length();
        if(searchLen>0){
            final int index=this.indexOf(searchStr,0);
            if(index>=0){
                final int replaceLen=(replaceStr==null)?0:replaceStr.length();
                this.replaceImpl(index,index+searchLen,searchLen,replaceStr,replaceLen);
            }
        }
        return this;
    }
    public StrBuilder replaceAll(final StrMatcher matcher,final String replaceStr){
        return this.replace(matcher,replaceStr,0,this.size,-1);
    }
    public StrBuilder replaceFirst(final StrMatcher matcher,final String replaceStr){
        return this.replace(matcher,replaceStr,0,this.size,1);
    }
    public StrBuilder replace(final StrMatcher matcher,final String replaceStr,final int startIndex,int endIndex,final int replaceCount){
        endIndex=this.validateRange(startIndex,endIndex);
        return this.replaceImpl(matcher,replaceStr,startIndex,endIndex,replaceCount);
    }
    private StrBuilder replaceImpl(final StrMatcher matcher,final String replaceStr,final int from,int to,int replaceCount){
        if(matcher==null||this.size==0){
            return this;
        }
        final int replaceLen=(replaceStr==null)?0:replaceStr.length();
        final char[] buf=this.buffer;
        for(int i=from;i<to&&replaceCount!=0;++i){
            final int removeLen=matcher.isMatch(buf,i,from,to);
            if(removeLen>0){
                this.replaceImpl(i,i+removeLen,removeLen,replaceStr,replaceLen);
                to=to-removeLen+replaceLen;
                i=i+replaceLen-1;
                if(replaceCount>0){
                    --replaceCount;
                }
            }
        }
        return this;
    }
    public StrBuilder reverse(){
        if(this.size==0){
            return this;
        }
        final int half=this.size/2;
        final char[] buf=this.buffer;
        for(int leftIdx=0,rightIdx=this.size-1;leftIdx<half;++leftIdx,--rightIdx){
            final char swap=buf[leftIdx];
            buf[leftIdx]=buf[rightIdx];
            buf[rightIdx]=swap;
        }
        return this;
    }
    public StrBuilder trim(){
        if(this.size==0){
            return this;
        }
        int len;
        char[] buf;
        int pos;
        for(len=this.size,buf=this.buffer,pos=0;pos<len&&buf[pos]<=' ';++pos){
        }
        while(pos<len&&buf[len-1]<=' '){
            --len;
        }
        if(len<this.size){
            this.delete(len,this.size);
        }
        if(pos>0){
            this.delete(0,pos);
        }
        return this;
    }
    public boolean startsWith(final String str){
        if(str==null){
            return false;
        }
        final int len=str.length();
        if(len==0){
            return true;
        }
        if(len>this.size){
            return false;
        }
        for(int i=0;i<len;++i){
            if(this.buffer[i]!=str.charAt(i)){
                return false;
            }
        }
        return true;
    }
    public boolean endsWith(final String str){
        if(str==null){
            return false;
        }
        final int len=str.length();
        if(len==0){
            return true;
        }
        if(len>this.size){
            return false;
        }
        for(int pos=this.size-len,i=0;i<len;++i,++pos){
            if(this.buffer[pos]!=str.charAt(i)){
                return false;
            }
        }
        return true;
    }
    public String substring(final int start){
        return this.substring(start,this.size);
    }
    public String substring(final int startIndex,int endIndex){
        endIndex=this.validateRange(startIndex,endIndex);
        return new String(this.buffer,startIndex,endIndex-startIndex);
    }
    public String leftString(final int length){
        if(length<=0){
            return "";
        }
        if(length>=this.size){
            return new String(this.buffer,0,this.size);
        }
        return new String(this.buffer,0,length);
    }
    public String rightString(final int length){
        if(length<=0){
            return "";
        }
        if(length>=this.size){
            return new String(this.buffer,0,this.size);
        }
        return new String(this.buffer,this.size-length,length);
    }
    public String midString(int index,final int length){
        if(index<0){
            index=0;
        }
        if(length<=0||index>=this.size){
            return "";
        }
        if(this.size<=index+length){
            return new String(this.buffer,index,this.size-index);
        }
        return new String(this.buffer,index,length);
    }
    public boolean contains(final char ch){
        final char[] thisBuf=this.buffer;
        for(int i=0;i<this.size;++i){
            if(thisBuf[i]==ch){
                return true;
            }
        }
        return false;
    }
    public boolean contains(final String str){
        return this.indexOf(str,0)>=0;
    }
    public boolean contains(final StrMatcher matcher){
        return this.indexOf(matcher,0)>=0;
    }
    public int indexOf(final char ch){
        return this.indexOf(ch,0);
    }
    public int indexOf(final char ch,int startIndex){
        startIndex=((startIndex<0)?0:startIndex);
        if(startIndex>=this.size){
            return -1;
        }
        final char[] thisBuf=this.buffer;
        for(int i=startIndex;i<this.size;++i){
            if(thisBuf[i]==ch){
                return i;
            }
        }
        return -1;
    }
    public int indexOf(final String str){
        return this.indexOf(str,0);
    }
    public int indexOf(final String str,int startIndex){
        startIndex=((startIndex<0)?0:startIndex);
        if(str==null||startIndex>=this.size){
            return -1;
        }
        final int strLen=str.length();
        if(strLen==1){
            return this.indexOf(str.charAt(0),startIndex);
        }
        if(strLen==0){
            return startIndex;
        }
        if(strLen>this.size){
            return -1;
        }
        final char[] thisBuf=this.buffer;
        final int len=this.size-strLen+1;
        int i=startIndex;
    Label_0080:
        while(i<len){
            for(int j=0;j<strLen;++j){
                if(str.charAt(j)!=thisBuf[i+j]){
                    ++i;
                    continue Label_0080;
                }
            }
            return i;
        }
        return -1;
    }
    public int indexOf(final StrMatcher matcher){
        return this.indexOf(matcher,0);
    }
    public int indexOf(final StrMatcher matcher,int startIndex){
        startIndex=((startIndex<0)?0:startIndex);
        if(matcher==null||startIndex>=this.size){
            return -1;
        }
        final int len=this.size;
        final char[] buf=this.buffer;
        for(int i=startIndex;i<len;++i){
            if(matcher.isMatch(buf,i,startIndex,len)>0){
                return i;
            }
        }
        return -1;
    }
    public int lastIndexOf(final char ch){
        return this.lastIndexOf(ch,this.size-1);
    }
    public int lastIndexOf(final char ch,int startIndex){
        startIndex=((startIndex>=this.size)?(this.size-1):startIndex);
        if(startIndex<0){
            return -1;
        }
        for(int i=startIndex;i>=0;--i){
            if(this.buffer[i]==ch){
                return i;
            }
        }
        return -1;
    }
    public int lastIndexOf(final String str){
        return this.lastIndexOf(str,this.size-1);
    }
    public int lastIndexOf(final String str,int startIndex){
        startIndex=((startIndex>=this.size)?(this.size-1):startIndex);
        if(str==null||startIndex<0){
            return -1;
        }
        final int strLen=str.length();
        if(strLen>0&&strLen<=this.size){
            if(strLen==1){
                return this.lastIndexOf(str.charAt(0),startIndex);
            }
            int i=startIndex-strLen+1;
        Label_0069:
            while(i>=0){
                for(int j=0;j<strLen;++j){
                    if(str.charAt(j)!=this.buffer[i+j]){
                        --i;
                        continue Label_0069;
                    }
                }
                return i;
            }
        }
        else if(strLen==0){
            return startIndex;
        }
        return -1;
    }
    public int lastIndexOf(final StrMatcher matcher){
        return this.lastIndexOf(matcher,this.size);
    }
    public int lastIndexOf(final StrMatcher matcher,int startIndex){
        startIndex=((startIndex>=this.size)?(this.size-1):startIndex);
        if(matcher==null||startIndex<0){
            return -1;
        }
        final char[] buf=this.buffer;
        final int endIndex=startIndex+1;
        for(int i=startIndex;i>=0;--i){
            if(matcher.isMatch(buf,i,0,endIndex)>0){
                return i;
            }
        }
        return -1;
    }
    public StrTokenizer asTokenizer(){
        return new StrBuilderTokenizer();
    }
    public Reader asReader(){
        return new StrBuilderReader();
    }
    public Writer asWriter(){
        return new StrBuilderWriter();
    }
    public boolean equalsIgnoreCase(final StrBuilder other){
        if(this==other){
            return true;
        }
        if(this.size!=other.size){
            return false;
        }
        final char[] thisBuf=this.buffer;
        final char[] otherBuf=other.buffer;
        for(int i=this.size-1;i>=0;--i){
            final char c1=thisBuf[i];
            final char c2=otherBuf[i];
            if(c1!=c2&&Character.toUpperCase(c1)!=Character.toUpperCase(c2)){
                return false;
            }
        }
        return true;
    }
    public boolean equals(final StrBuilder other){
        if(this==other){
            return true;
        }
        if(this.size!=other.size){
            return false;
        }
        final char[] thisBuf=this.buffer;
        final char[] otherBuf=other.buffer;
        for(int i=this.size-1;i>=0;--i){
            if(thisBuf[i]!=otherBuf[i]){
                return false;
            }
        }
        return true;
    }
    public boolean equals(final Object obj){
        return obj instanceof StrBuilder&&this.equals((StrBuilder)obj);
    }
    public int hashCode(){
        final char[] buf=this.buffer;
        int hash=0;
        for(int i=this.size-1;i>=0;--i){
            hash=31*hash+buf[i];
        }
        return hash;
    }
    public String toString(){
        return new String(this.buffer,0,this.size);
    }
    public StringBuffer toStringBuffer(){
        return new StringBuffer(this.size).append(this.buffer,0,this.size);
    }
    protected int validateRange(final int startIndex,int endIndex){
        if(startIndex<0){
            throw new StringIndexOutOfBoundsException(startIndex);
        }
        if(endIndex>this.size){
            endIndex=this.size;
        }
        if(startIndex>endIndex){
            throw new StringIndexOutOfBoundsException("end < start");
        }
        return endIndex;
    }
    protected void validateIndex(final int index){
        if(index<0||index>this.size){
            throw new StringIndexOutOfBoundsException(index);
        }
    }
    class StrBuilderTokenizer extends StrTokenizer{
        protected List tokenize(final char[] chars,final int offset,final int count){
            if(chars==null){
                return super.tokenize(StrBuilder.this.buffer,0,StrBuilder.this.size());
            }
            return super.tokenize(chars,offset,count);
        }
        public String getContent(){
            final String str=super.getContent();
            if(str==null){
                return StrBuilder.this.toString();
            }
            return str;
        }
    }
    class StrBuilderReader extends Reader{
        private int pos;
        private int mark;
        public void close(){
        }
        public int read(){
            if(!this.ready()){
                return -1;
            }
            return StrBuilder.this.charAt(this.pos++);
        }
        public int read(final char[] b,final int off,int len){
            if(off<0||len<0||off>b.length||off+len>b.length||off+len<0){
                throw new IndexOutOfBoundsException();
            }
            if(len==0){
                return 0;
            }
            if(this.pos>=StrBuilder.this.size()){
                return -1;
            }
            if(this.pos+len>StrBuilder.this.size()){
                len=StrBuilder.this.size()-this.pos;
            }
            StrBuilder.this.getChars(this.pos,this.pos+len,b,off);
            this.pos+=len;
            return len;
        }
        public long skip(long n){
            if(this.pos+n>StrBuilder.this.size()){
                n=StrBuilder.this.size()-this.pos;
            }
            if(n<0L){
                return 0L;
            }
            this.pos+=(int)n;
            return n;
        }
        public boolean ready(){
            return this.pos<StrBuilder.this.size();
        }
        public boolean markSupported(){
            return true;
        }
        public void mark(final int readAheadLimit){
            this.mark=this.pos;
        }
        public void reset(){
            this.pos=this.mark;
        }
    }
    class StrBuilderWriter extends Writer{
        public void close(){
        }
        public void flush(){
        }
        public void write(final int c){
            StrBuilder.this.append((char)c);
        }
        public void write(final char[] cbuf){
            StrBuilder.this.append(cbuf);
        }
        public void write(final char[] cbuf,final int off,final int len){
            StrBuilder.this.append(cbuf,off,len);
        }
        public void write(final String str){
            StrBuilder.this.append(str);
        }
        public void write(final String str,final int off,final int len){
            StrBuilder.this.append(str,off,len);
        }
    }
}
