package org.apache.log4j.helpers;

import java.text.SimpleDateFormat;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import org.apache.log4j.helpers.RelativeTimeDateFormat;
import org.apache.log4j.spi.LoggingEvent;
import java.util.TimeZone;
import java.util.Date;
import java.text.DateFormat;
import java.text.FieldPosition;
import org.apache.log4j.Layout;

public abstract class DateLayout extends Layout{
    public static final String NULL_DATE_FORMAT="NULL";
    public static final String RELATIVE_TIME_DATE_FORMAT="RELATIVE";
    protected FieldPosition pos;
    public static final String DATE_FORMAT_OPTION="DateFormat";
    public static final String TIMEZONE_OPTION="TimeZone";
    private String timeZoneID;
    private String dateFormatOption;
    protected DateFormat dateFormat;
    protected Date date;
    public DateLayout(){
        super();
        this.pos=new FieldPosition(0);
        this.date=new Date();
    }
    public String[] getOptionStrings(){
        return new String[] { "DateFormat","TimeZone" };
    }
    public void setOption(final String option,final String value){
        if(option.equalsIgnoreCase("DateFormat")){
            this.dateFormatOption=value.toUpperCase();
        }
        else if(option.equalsIgnoreCase("TimeZone")){
            this.timeZoneID=value;
        }
    }
    public void setDateFormat(final String dateFormat){
        if(dateFormat!=null){
            this.dateFormatOption=dateFormat;
        }
        this.setDateFormat(this.dateFormatOption,TimeZone.getDefault());
    }
    public String getDateFormat(){
        return this.dateFormatOption;
    }
    public void setTimeZone(final String timeZone){
        this.timeZoneID=timeZone;
    }
    public String getTimeZone(){
        return this.timeZoneID;
    }
    public void activateOptions(){
        this.setDateFormat(this.dateFormatOption);
        if(this.timeZoneID!=null&&this.dateFormat!=null){
            this.dateFormat.setTimeZone(TimeZone.getTimeZone(this.timeZoneID));
        }
    }
    public void dateFormat(final StringBuffer buf,final LoggingEvent event){
        if(this.dateFormat!=null){
            this.date.setTime(event.timeStamp);
            this.dateFormat.format(this.date,buf,this.pos);
            buf.append(' ');
        }
    }
    public void setDateFormat(final DateFormat dateFormat,final TimeZone timeZone){
        (this.dateFormat=dateFormat).setTimeZone(timeZone);
    }
    public void setDateFormat(final String dateFormatType,final TimeZone timeZone){
        if(dateFormatType==null){
            this.dateFormat=null;
            return;
        }
        if(dateFormatType.equalsIgnoreCase("NULL")){
            this.dateFormat=null;
        }
        else if(dateFormatType.equalsIgnoreCase("RELATIVE")){
            this.dateFormat=new RelativeTimeDateFormat();
        }
        else if(dateFormatType.equalsIgnoreCase("ABSOLUTE")){
            this.dateFormat=new AbsoluteTimeDateFormat(timeZone);
        }
        else if(dateFormatType.equalsIgnoreCase("DATE")){
            this.dateFormat=new DateTimeDateFormat(timeZone);
        }
        else if(dateFormatType.equalsIgnoreCase("ISO8601")){
            this.dateFormat=new ISO8601DateFormat(timeZone);
        }
        else{
            (this.dateFormat=new SimpleDateFormat(dateFormatType)).setTimeZone(timeZone);
        }
    }
}
