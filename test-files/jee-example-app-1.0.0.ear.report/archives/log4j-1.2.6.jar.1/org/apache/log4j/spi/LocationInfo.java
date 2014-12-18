package org.apache.log4j.spi;

import org.apache.log4j.helpers.LogLog;
import java.io.Writer;
import org.apache.log4j.Layout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Serializable;

public class LocationInfo implements Serializable{
    transient String lineNumber;
    transient String fileName;
    transient String className;
    transient String methodName;
    public String fullInfo;
    private static StringWriter sw;
    private static PrintWriter pw;
    public static final String NA="?";
    static final long serialVersionUID=-1325822038990805636L;
    static boolean inVisualAge;
    public LocationInfo(final Throwable t,final String fqnOfCallingClass){
        super();
        if(t==null){
            return;
        }
        final String s;
        synchronized(LocationInfo.sw){
            t.printStackTrace(LocationInfo.pw);
            s=LocationInfo.sw.toString();
            LocationInfo.sw.getBuffer().setLength(0);
        }
        int ibegin=s.lastIndexOf(fqnOfCallingClass);
        if(ibegin==-1){
            return;
        }
        ibegin=s.indexOf(Layout.LINE_SEP,ibegin);
        if(ibegin==-1){
            return;
        }
        ibegin+=Layout.LINE_SEP_LEN;
        final int iend=s.indexOf(Layout.LINE_SEP,ibegin);
        if(iend==-1){
            return;
        }
        if(!LocationInfo.inVisualAge){
            ibegin=s.lastIndexOf("at ",iend);
            if(ibegin==-1){
                return;
            }
            ibegin+=3;
        }
        this.fullInfo=s.substring(ibegin,iend);
    }
    public String getClassName(){
        if(this.fullInfo==null){
            return "?";
        }
        if(this.className==null){
            int iend=this.fullInfo.lastIndexOf(40);
            if(iend==-1){
                this.className="?";
            }
            else{
                iend=this.fullInfo.lastIndexOf(46,iend);
                int ibegin=0;
                if(LocationInfo.inVisualAge){
                    ibegin=this.fullInfo.lastIndexOf(32,iend)+1;
                }
                if(iend==-1){
                    this.className="?";
                }
                else{
                    this.className=this.fullInfo.substring(ibegin,iend);
                }
            }
        }
        return this.className;
    }
    public String getFileName(){
        if(this.fullInfo==null){
            return "?";
        }
        if(this.fileName==null){
            final int iend=this.fullInfo.lastIndexOf(58);
            if(iend==-1){
                this.fileName="?";
            }
            else{
                final int ibegin=this.fullInfo.lastIndexOf(40,iend-1);
                this.fileName=this.fullInfo.substring(ibegin+1,iend);
            }
        }
        return this.fileName;
    }
    public String getLineNumber(){
        if(this.fullInfo==null){
            return "?";
        }
        if(this.lineNumber==null){
            final int iend=this.fullInfo.lastIndexOf(41);
            final int ibegin=this.fullInfo.lastIndexOf(58,iend-1);
            if(ibegin==-1){
                this.lineNumber="?";
            }
            else{
                this.lineNumber=this.fullInfo.substring(ibegin+1,iend);
            }
        }
        return this.lineNumber;
    }
    public String getMethodName(){
        if(this.fullInfo==null){
            return "?";
        }
        if(this.methodName==null){
            final int iend=this.fullInfo.lastIndexOf(40);
            final int ibegin=this.fullInfo.lastIndexOf(46,iend);
            if(ibegin==-1){
                this.methodName="?";
            }
            else{
                this.methodName=this.fullInfo.substring(ibegin+1,iend);
            }
        }
        return this.methodName;
    }
    static{
        LocationInfo.sw=new StringWriter();
        LocationInfo.pw=new PrintWriter(LocationInfo.sw);
        LocationInfo.inVisualAge=false;
        try{
            final Class dummy=Class.forName("com.ibm.uvm.tools.DebugSupport");
            LocationInfo.inVisualAge=true;
            LogLog.debug("Detected IBM VisualAge environment.");
        }
        catch(Throwable t){
        }
    }
}
