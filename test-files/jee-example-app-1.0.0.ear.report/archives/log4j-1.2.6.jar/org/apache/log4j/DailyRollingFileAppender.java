package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;
import java.util.Locale;
import org.apache.log4j.helpers.LogLog;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Layout;
import java.util.TimeZone;
import org.apache.log4j.RollingCalendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.FileAppender;

public class DailyRollingFileAppender extends FileAppender{
    static final int TOP_OF_TROUBLE=-1;
    static final int TOP_OF_MINUTE=0;
    static final int TOP_OF_HOUR=1;
    static final int HALF_DAY=2;
    static final int TOP_OF_DAY=3;
    static final int TOP_OF_WEEK=4;
    static final int TOP_OF_MONTH=5;
    private String datePattern;
    private String scheduledFilename;
    private long nextCheck;
    Date now;
    SimpleDateFormat sdf;
    RollingCalendar rc;
    int checkPeriod;
    static final TimeZone gmtTimeZone;
    public DailyRollingFileAppender(){
        super();
        this.datePattern="'.'yyyy-MM-dd";
        this.nextCheck=System.currentTimeMillis()-1L;
        this.now=new Date();
        this.rc=new RollingCalendar();
        this.checkPeriod=-1;
    }
    public DailyRollingFileAppender(final Layout layout,final String filename,final String datePattern) throws IOException{
        super(layout,filename,true);
        this.datePattern="'.'yyyy-MM-dd";
        this.nextCheck=System.currentTimeMillis()-1L;
        this.now=new Date();
        this.rc=new RollingCalendar();
        this.checkPeriod=-1;
        this.datePattern=datePattern;
        this.activateOptions();
    }
    public void setDatePattern(final String pattern){
        this.datePattern=pattern;
    }
    public String getDatePattern(){
        return this.datePattern;
    }
    public void activateOptions(){
        super.activateOptions();
        if(this.datePattern!=null&&super.fileName!=null){
            this.now.setTime(System.currentTimeMillis());
            this.sdf=new SimpleDateFormat(this.datePattern);
            final int type=this.computeCheckPeriod();
            this.printPeriodicity(type);
            this.rc.setType(type);
            final File file=new File(super.fileName);
            this.scheduledFilename=super.fileName+this.sdf.format(new Date(file.lastModified()));
        }
        else{
            LogLog.error("Either File or DatePattern options are not set for appender ["+super.name+"].");
        }
    }
    void printPeriodicity(final int type){
        switch(type){
            case 0:{
                LogLog.debug("Appender ["+super.name+"] to be rolled every minute.");
                break;
            }
            case 1:{
                LogLog.debug("Appender ["+super.name+"] to be rolled on top of every hour.");
                break;
            }
            case 2:{
                LogLog.debug("Appender ["+super.name+"] to be rolled at midday and midnight.");
                break;
            }
            case 3:{
                LogLog.debug("Appender ["+super.name+"] to be rolled at midnight.");
                break;
            }
            case 4:{
                LogLog.debug("Appender ["+super.name+"] to be rolled at start of week.");
                break;
            }
            case 5:{
                LogLog.debug("Appender ["+super.name+"] to be rolled at start of every month.");
                break;
            }
            default:{
                LogLog.warn("Unknown periodicity for appender ["+super.name+"].");
                break;
            }
        }
    }
    int computeCheckPeriod(){
        final RollingCalendar rollingCalendar=new RollingCalendar(DailyRollingFileAppender.gmtTimeZone,Locale.ENGLISH);
        final Date epoch=new Date(0L);
        if(this.datePattern!=null){
            for(int i=0;i<=5;++i){
                final SimpleDateFormat simpleDateFormat=new SimpleDateFormat(this.datePattern);
                simpleDateFormat.setTimeZone(DailyRollingFileAppender.gmtTimeZone);
                final String r0=simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                final Date next=new Date(rollingCalendar.getNextCheckMillis(epoch));
                final String r=simpleDateFormat.format(next);
                if(r0!=null&&r!=null&&!r0.equals(r)){
                    return i;
                }
            }
        }
        return -1;
    }
    void rollOver() throws IOException{
        if(this.datePattern==null){
            super.errorHandler.error("Missing DatePattern option in rollOver().");
            return;
        }
        final String datedFilename=super.fileName+this.sdf.format(this.now);
        if(this.scheduledFilename.equals(datedFilename)){
            return;
        }
        this.closeFile();
        final File target=new File(this.scheduledFilename);
        if(target.exists()){
            target.delete();
        }
        final File file=new File(super.fileName);
        final boolean result=file.renameTo(target);
        if(result){
            LogLog.debug(super.fileName+" -> "+this.scheduledFilename);
        }
        else{
            LogLog.error("Failed to rename ["+super.fileName+"] to ["+this.scheduledFilename+"].");
        }
        try{
            this.setFile(super.fileName,false,super.bufferedIO,super.bufferSize);
        }
        catch(IOException e){
            super.errorHandler.error("setFile("+super.fileName+", false) call failed.");
        }
        this.scheduledFilename=datedFilename;
    }
    protected void subAppend(final LoggingEvent event){
        final long n=System.currentTimeMillis();
        if(n>=this.nextCheck){
            this.now.setTime(n);
            this.nextCheck=this.rc.getNextCheckMillis(this.now);
            try{
                this.rollOver();
            }
            catch(IOException ioe){
                LogLog.error("rollOver() failed.",ioe);
            }
        }
        super.subAppend(event);
    }
    static{
        gmtTimeZone=TimeZone.getTimeZone("GMT");
    }
}
