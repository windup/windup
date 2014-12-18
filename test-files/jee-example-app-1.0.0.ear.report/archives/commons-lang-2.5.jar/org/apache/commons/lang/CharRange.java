package org.apache.commons.lang;

import java.util.NoSuchElementException;
import java.util.Iterator;
import java.io.Serializable;

public final class CharRange implements Serializable{
    private static final long serialVersionUID=8270183163158333422L;
    private final char start;
    private final char end;
    private final boolean negated;
    private transient String iToString;
    public static CharRange is(final char ch){
        return new CharRange(ch,ch,false);
    }
    public static CharRange isNot(final char ch){
        return new CharRange(ch,ch,true);
    }
    public static CharRange isIn(final char start,final char end){
        return new CharRange(start,end,false);
    }
    public static CharRange isNotIn(final char start,final char end){
        return new CharRange(start,end,true);
    }
    public CharRange(final char ch){
        this(ch,ch,false);
    }
    public CharRange(final char ch,final boolean negated){
        this(ch,ch,negated);
    }
    public CharRange(final char start,final char end){
        this(start,end,false);
    }
    public CharRange(char start,char end,final boolean negated){
        super();
        if(start>end){
            final char temp=start;
            start=end;
            end=temp;
        }
        this.start=start;
        this.end=end;
        this.negated=negated;
    }
    public char getStart(){
        return this.start;
    }
    public char getEnd(){
        return this.end;
    }
    public boolean isNegated(){
        return this.negated;
    }
    public boolean contains(final char ch){
        return (ch>=this.start&&ch<=this.end)!=this.negated;
    }
    public boolean contains(final CharRange range){
        if(range==null){
            throw new IllegalArgumentException("The Range must not be null");
        }
        if(this.negated){
            if(range.negated){
                return this.start>=range.start&&this.end<=range.end;
            }
            return range.end<this.start||range.start>this.end;
        }
        else{
            if(range.negated){
                return this.start=='\0'&&this.end=='\uffff';
            }
            return this.start<=range.start&&this.end>=range.end;
        }
    }
    public boolean equals(final Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof CharRange)){
            return false;
        }
        final CharRange other=(CharRange)obj;
        return this.start==other.start&&this.end==other.end&&this.negated==other.negated;
    }
    public int hashCode(){
        return 'S'+this.start+'\u0007'*this.end+(this.negated?1:0);
    }
    public String toString(){
        if(this.iToString==null){
            final StringBuffer buf=new StringBuffer(4);
            if(this.isNegated()){
                buf.append('^');
            }
            buf.append(this.start);
            if(this.start!=this.end){
                buf.append('-');
                buf.append(this.end);
            }
            this.iToString=buf.toString();
        }
        return this.iToString;
    }
    public Iterator iterator(){
        return new CharacterIterator(this);
    }
    private static class CharacterIterator implements Iterator{
        private char current;
        private CharRange range;
        private boolean hasNext;
        private CharacterIterator(final CharRange r){
            super();
            this.range=r;
            this.hasNext=true;
            if(this.range.negated){
                if(this.range.start=='\0'){
                    if(this.range.end=='\uffff'){
                        this.hasNext=false;
                    }
                    else{
                        this.current=(char)(this.range.end+'\u0001');
                    }
                }
                else{
                    this.current='\0';
                }
            }
            else{
                this.current=this.range.start;
            }
        }
        private void prepareNext(){
            if(this.range.negated){
                if(this.current=='\uffff'){
                    this.hasNext=false;
                }
                else if(this.current+'\u0001'==this.range.start){
                    if(this.range.end=='\uffff'){
                        this.hasNext=false;
                    }
                    else{
                        this.current=(char)(this.range.end+'\u0001');
                    }
                }
                else{
                    ++this.current;
                }
            }
            else if(this.current<this.range.end){
                ++this.current;
            }
            else{
                this.hasNext=false;
            }
        }
        public boolean hasNext(){
            return this.hasNext;
        }
        public Object next(){
            if(!this.hasNext){
                throw new NoSuchElementException();
            }
            final char cur=this.current;
            this.prepareNext();
            return new Character(cur);
        }
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }
}
