package org.apache.log4j.helpers;

import java.text.ParsePosition;
import java.text.FieldPosition;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.DateFormat;

public class AbsoluteTimeDateFormat extends DateFormat{
    public static final String ABS_TIME_DATE_FORMAT="ABSOLUTE";
    public static final String DATE_AND_TIME_DATE_FORMAT="DATE";
    public static final String ISO8601_DATE_FORMAT="ISO8601";
    private static long previousTime;
    private static char[] previousTimeWithoutMillis;
    public AbsoluteTimeDateFormat(){
        super();
        this.setCalendar(Calendar.getInstance());
    }
    public AbsoluteTimeDateFormat(final TimeZone timeZone){
        super();
        this.setCalendar(Calendar.getInstance(timeZone));
    }
    public StringBuffer format(final Date date,final StringBuffer sbuf,final FieldPosition fieldPosition){
        final long now=date.getTime();
        final int millis=(int)(now%1000L);
        if(now-millis!=AbsoluteTimeDateFormat.previousTime){
            super.calendar.setTime(date);
            final int start=sbuf.length();
            final int hour=super.calendar.get(11);
            if(hour<10){
                sbuf.append('0');
            }
            sbuf.append(hour);
            sbuf.append(':');
            final int mins=super.calendar.get(12);
            if(mins<10){
                sbuf.append('0');
            }
            sbuf.append(mins);
            sbuf.append(':');
            final int secs=super.calendar.get(13);
            if(secs<10){
                sbuf.append('0');
            }
            sbuf.append(secs);
            sbuf.append(',');
            sbuf.getChars(start,sbuf.length(),AbsoluteTimeDateFormat.previousTimeWithoutMillis,0);
            AbsoluteTimeDateFormat.previousTime=now-millis;
        }
        else{
            sbuf.append(AbsoluteTimeDateFormat.previousTimeWithoutMillis);
        }
        if(millis<100){
            sbuf.append('0');
        }
        if(millis<10){
            sbuf.append('0');
        }
        sbuf.append(millis);
        return sbuf;
    }
    public Date parse(final String s,final ParsePosition pos){
        return null;
    }
    static{
        AbsoluteTimeDateFormat.previousTimeWithoutMillis=new char[9];
    }
}
