package org.apache.log4j.helpers;

import org.apache.log4j.helpers.LogLog;

public class FormattingInfo{
    int min;
    int max;
    boolean leftAlign;
    public FormattingInfo(){
        super();
        this.min=-1;
        this.max=Integer.MAX_VALUE;
        this.leftAlign=false;
    }
    void reset(){
        this.min=-1;
        this.max=Integer.MAX_VALUE;
        this.leftAlign=false;
    }
    void dump(){
        LogLog.debug("min="+this.min+", max="+this.max+", leftAlign="+this.leftAlign);
    }
}
