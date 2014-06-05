package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.*;
import org.apache.wicket.util.time.*;

public class UploadInfo implements IClusterable{
    private static final long serialVersionUID=1L;
    private transient long timeStarted;
    private transient long totalBytes;
    private transient long bytesUploaded;
    public UploadInfo(final int totalBytes){
        super();
        this.timeStarted=System.currentTimeMillis();
        this.totalBytes=totalBytes;
    }
    public long getBytesUploaded(){
        return this.bytesUploaded;
    }
    public void setBytesUploaded(final long bytesUploaded){
        this.bytesUploaded=bytesUploaded;
    }
    public String getBytesUploadedString(){
        return Bytes.bytes(this.bytesUploaded).toString(Session.get().getLocale());
    }
    public String getTotalBytesString(){
        return Bytes.bytes(this.totalBytes).toString(Session.get().getLocale());
    }
    public long getTotalBytes(){
        return this.totalBytes;
    }
    public long getElapsedMilliseconds(){
        return System.currentTimeMillis()-this.timeStarted;
    }
    public long getElapsedSeconds(){
        return this.getElapsedMilliseconds()/1000L;
    }
    public long getTransferRateBPS(){
        return this.bytesUploaded/Math.max(this.getElapsedSeconds(),1L);
    }
    public String getTransferRateString(){
        return Bytes.bytes(this.getTransferRateBPS()).toString(Session.get().getLocale())+"/s";
    }
    public int getPercentageComplete(){
        if(this.totalBytes==0L){
            return 100;
        }
        return (int)(this.bytesUploaded/this.totalBytes*100.0);
    }
    public long getRemainingMilliseconds(){
        final int percentageComplete=this.getPercentageComplete();
        final long totalTime=this.getElapsedSeconds()*100L/Math.max(percentageComplete,1);
        final long remainingTime=totalTime-this.getElapsedSeconds();
        return remainingTime*1000L;
    }
    public String getRemainingTimeString(){
        return Duration.milliseconds(this.getRemainingMilliseconds()).toString(Session.get().getLocale());
    }
}
