package org.apache.log4j.net;

import java.io.Writer;
import org.apache.log4j.helpers.SyslogWriter;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.SyslogQuietWriter;
import org.apache.log4j.AppenderSkeleton;

public class SyslogAppender extends AppenderSkeleton{
    public static final int LOG_KERN=0;
    public static final int LOG_USER=8;
    public static final int LOG_MAIL=16;
    public static final int LOG_DAEMON=24;
    public static final int LOG_AUTH=32;
    public static final int LOG_SYSLOG=40;
    public static final int LOG_LPR=48;
    public static final int LOG_NEWS=56;
    public static final int LOG_UUCP=64;
    public static final int LOG_CRON=72;
    public static final int LOG_AUTHPRIV=80;
    public static final int LOG_FTP=88;
    public static final int LOG_LOCAL0=128;
    public static final int LOG_LOCAL1=136;
    public static final int LOG_LOCAL2=144;
    public static final int LOG_LOCAL3=152;
    public static final int LOG_LOCAL4=160;
    public static final int LOG_LOCAL5=168;
    public static final int LOG_LOCAL6=176;
    public static final int LOG_LOCAL7=184;
    protected static final int SYSLOG_HOST_OI=0;
    protected static final int FACILITY_OI=1;
    static final String TAB="    ";
    int syslogFacility;
    String facilityStr;
    boolean facilityPrinting;
    SyslogQuietWriter sqw;
    String syslogHost;
    public SyslogAppender(){
        super();
        this.syslogFacility=8;
        this.facilityPrinting=false;
        this.initSyslogFacilityStr();
    }
    public SyslogAppender(final Layout layout,final int syslogFacility){
        super();
        this.syslogFacility=8;
        this.facilityPrinting=false;
        super.layout=layout;
        this.syslogFacility=syslogFacility;
        this.initSyslogFacilityStr();
    }
    public SyslogAppender(final Layout layout,final String syslogHost,final int syslogFacility){
        this(layout,syslogFacility);
        this.setSyslogHost(syslogHost);
    }
    public synchronized void close(){
        super.closed=true;
        this.sqw=null;
    }
    private void initSyslogFacilityStr(){
        this.facilityStr=getFacilityString(this.syslogFacility);
        if(this.facilityStr==null){
            System.err.println("\""+this.syslogFacility+"\" is an unknown syslog facility. Defaulting to \"USER\".");
            this.syslogFacility=8;
            this.facilityStr="user:";
        }
        else{
            this.facilityStr+=":";
        }
    }
    public static String getFacilityString(final int syslogFacility){
        switch(syslogFacility){
            case 0:{
                return "kern";
            }
            case 8:{
                return "user";
            }
            case 16:{
                return "mail";
            }
            case 24:{
                return "daemon";
            }
            case 32:{
                return "auth";
            }
            case 40:{
                return "syslog";
            }
            case 48:{
                return "lpr";
            }
            case 56:{
                return "news";
            }
            case 64:{
                return "uucp";
            }
            case 72:{
                return "cron";
            }
            case 80:{
                return "authpriv";
            }
            case 88:{
                return "ftp";
            }
            case 128:{
                return "local0";
            }
            case 136:{
                return "local1";
            }
            case 144:{
                return "local2";
            }
            case 152:{
                return "local3";
            }
            case 160:{
                return "local4";
            }
            case 168:{
                return "local5";
            }
            case 176:{
                return "local6";
            }
            case 184:{
                return "local7";
            }
            default:{
                return null;
            }
        }
    }
    public static int getFacility(String facilityName){
        if(facilityName!=null){
            facilityName=facilityName.trim();
        }
        if("KERN".equalsIgnoreCase(facilityName)){
            return 0;
        }
        if("USER".equalsIgnoreCase(facilityName)){
            return 8;
        }
        if("MAIL".equalsIgnoreCase(facilityName)){
            return 16;
        }
        if("DAEMON".equalsIgnoreCase(facilityName)){
            return 24;
        }
        if("AUTH".equalsIgnoreCase(facilityName)){
            return 32;
        }
        if("SYSLOG".equalsIgnoreCase(facilityName)){
            return 40;
        }
        if("LPR".equalsIgnoreCase(facilityName)){
            return 48;
        }
        if("NEWS".equalsIgnoreCase(facilityName)){
            return 56;
        }
        if("UUCP".equalsIgnoreCase(facilityName)){
            return 64;
        }
        if("CRON".equalsIgnoreCase(facilityName)){
            return 72;
        }
        if("AUTHPRIV".equalsIgnoreCase(facilityName)){
            return 80;
        }
        if("FTP".equalsIgnoreCase(facilityName)){
            return 88;
        }
        if("LOCAL0".equalsIgnoreCase(facilityName)){
            return 128;
        }
        if("LOCAL1".equalsIgnoreCase(facilityName)){
            return 136;
        }
        if("LOCAL2".equalsIgnoreCase(facilityName)){
            return 144;
        }
        if("LOCAL3".equalsIgnoreCase(facilityName)){
            return 152;
        }
        if("LOCAL4".equalsIgnoreCase(facilityName)){
            return 160;
        }
        if("LOCAL5".equalsIgnoreCase(facilityName)){
            return 168;
        }
        if("LOCAL6".equalsIgnoreCase(facilityName)){
            return 176;
        }
        if("LOCAL7".equalsIgnoreCase(facilityName)){
            return 184;
        }
        return -1;
    }
    public void append(final LoggingEvent event){
        if(!this.isAsSevereAsThreshold(event.getLevel())){
            return;
        }
        if(this.sqw==null){
            super.errorHandler.error("No syslog host is set for SyslogAppedender named \""+super.name+"\".");
            return;
        }
        final String buffer=(this.facilityPrinting?this.facilityStr:"")+super.layout.format(event);
        this.sqw.setLevel(event.getLevel().getSyslogEquivalent());
        this.sqw.write(buffer);
        final String[] s=event.getThrowableStrRep();
        if(s!=null){
            final int len=s.length;
            if(len>0){
                this.sqw.write(s[0]);
                for(int i=1;i<len;++i){
                    this.sqw.write("    "+s[i].substring(1));
                }
            }
        }
    }
    public void activateOptions(){
    }
    public boolean requiresLayout(){
        return true;
    }
    public void setSyslogHost(final String syslogHost){
        this.sqw=new SyslogQuietWriter(new SyslogWriter(syslogHost),this.syslogFacility,super.errorHandler);
        this.syslogHost=syslogHost;
    }
    public String getSyslogHost(){
        return this.syslogHost;
    }
    public void setFacility(final String facilityName){
        if(facilityName==null){
            return;
        }
        this.syslogFacility=getFacility(facilityName);
        if(this.syslogFacility==-1){
            System.err.println("["+facilityName+"] is an unknown syslog facility. Defaulting to [USER].");
            this.syslogFacility=8;
        }
        this.initSyslogFacilityStr();
        if(this.sqw!=null){
            this.sqw.setSyslogFacility(this.syslogFacility);
        }
    }
    public String getFacility(){
        return getFacilityString(this.syslogFacility);
    }
    public void setFacilityPrinting(final boolean on){
        this.facilityPrinting=on;
    }
    public boolean getFacilityPrinting(){
        return this.facilityPrinting;
    }
}
