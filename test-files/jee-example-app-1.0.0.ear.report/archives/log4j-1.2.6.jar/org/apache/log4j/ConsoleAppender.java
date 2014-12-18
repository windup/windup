package org.apache.log4j;

import org.apache.log4j.helpers.LogLog;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;

public class ConsoleAppender extends WriterAppender{
    public static final String SYSTEM_OUT="System.out";
    public static final String SYSTEM_ERR="System.err";
    protected String target;
    public ConsoleAppender(){
        super();
        this.target="System.out";
    }
    public ConsoleAppender(final Layout layout){
        this(layout,"System.out");
    }
    public ConsoleAppender(final Layout layout,final String target){
        super();
        this.target="System.out";
        super.layout=layout;
        if("System.out".equals(target)){
            this.setWriter(new OutputStreamWriter(System.out));
        }
        else if("System.err".equalsIgnoreCase(target)){
            this.setWriter(new OutputStreamWriter(System.err));
        }
        else{
            this.targetWarn(target);
        }
    }
    public void setTarget(final String value){
        final String v=value.trim();
        if("System.out".equalsIgnoreCase(v)){
            this.target="System.out";
        }
        else if("System.err".equalsIgnoreCase(v)){
            this.target="System.err";
        }
        else{
            this.targetWarn(value);
        }
    }
    public String getTarget(){
        return this.target;
    }
    void targetWarn(final String val){
        LogLog.warn("["+val+"] should be System.out or System.err.");
        LogLog.warn("Using previously set target, System.out by default.");
    }
    public void activateOptions(){
        if(this.target.equals("System.out")){
            this.setWriter(new OutputStreamWriter(System.out));
        }
        else{
            this.setWriter(new OutputStreamWriter(System.err));
        }
    }
    protected final void closeWriter(){
    }
}
