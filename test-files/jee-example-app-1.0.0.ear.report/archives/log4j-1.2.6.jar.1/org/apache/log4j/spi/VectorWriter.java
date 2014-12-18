package org.apache.log4j.spi;

import java.io.Writer;
import org.apache.log4j.spi.NullWriter;
import java.util.Vector;
import java.io.PrintWriter;

class VectorWriter extends PrintWriter{
    private Vector v;
    VectorWriter(){
        super(new NullWriter());
        this.v=new Vector();
    }
    public void print(final Object o){
        this.v.addElement(o.toString());
    }
    public void print(final char[] chars){
        this.v.addElement(new String(chars));
    }
    public void print(final String s){
        this.v.addElement(s);
    }
    public void println(final Object o){
        this.v.addElement(o.toString());
    }
    public void println(final char[] chars){
        this.v.addElement(new String(chars));
    }
    public void println(final String s){
        this.v.addElement(s);
    }
    public void write(final char[] chars){
        this.v.addElement(new String(chars));
    }
    public void write(final char[] chars,final int off,final int len){
        this.v.addElement(new String(chars,off,len));
    }
    public void write(final String s,final int off,final int len){
        this.v.addElement(s.substring(off,off+len));
    }
    public void write(final String s){
        this.v.addElement(s);
    }
    public String[] toStringArray(){
        final int len=this.v.size();
        final String[] sa=new String[len];
        for(int i=0;i<len;++i){
            sa[i]=this.v.elementAt(i);
        }
        return sa;
    }
}
