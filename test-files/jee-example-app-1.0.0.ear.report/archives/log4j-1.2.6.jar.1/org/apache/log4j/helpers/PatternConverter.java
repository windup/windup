package org.apache.log4j.helpers;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.FormattingInfo;

public abstract class PatternConverter{
    public PatternConverter next;
    int min;
    int max;
    boolean leftAlign;
    static String[] SPACES;
    protected PatternConverter(){
        super();
        this.min=-1;
        this.max=Integer.MAX_VALUE;
        this.leftAlign=false;
    }
    protected PatternConverter(final FormattingInfo fi){
        super();
        this.min=-1;
        this.max=Integer.MAX_VALUE;
        this.leftAlign=false;
        this.min=fi.min;
        this.max=fi.max;
        this.leftAlign=fi.leftAlign;
    }
    protected abstract String convert(final LoggingEvent p0);
    public void format(final StringBuffer sbuf,final LoggingEvent e){
        final String s=this.convert(e);
        if(s==null){
            if(0<this.min){
                this.spacePad(sbuf,this.min);
            }
            return;
        }
        final int len=s.length();
        if(len>this.max){
            sbuf.append(s.substring(len-this.max));
        }
        else if(len<this.min){
            if(this.leftAlign){
                sbuf.append(s);
                this.spacePad(sbuf,this.min-len);
            }
            else{
                this.spacePad(sbuf,this.min-len);
                sbuf.append(s);
            }
        }
        else{
            sbuf.append(s);
        }
    }
    public void spacePad(final StringBuffer sbuf,int length){
        while(length>=32){
            sbuf.append(PatternConverter.SPACES[5]);
            length-=32;
        }
        for(int i=4;i>=0;--i){
            if((length&1<<i)!=0x0){
                sbuf.append(PatternConverter.SPACES[i]);
            }
        }
    }
    static{
        PatternConverter.SPACES=new String[] { " ","  ","    ","        ","                ","                                " };
    }
}
