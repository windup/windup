package org.apache.log4j.helpers;

import java.text.ParsePosition;
import java.text.FieldPosition;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.DateFormatSymbols;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;

public class DateTimeDateFormat extends AbsoluteTimeDateFormat{
    String[] shortMonths;
    public DateTimeDateFormat(){
        super();
        this.shortMonths=new DateFormatSymbols().getShortMonths();
    }
    public DateTimeDateFormat(final TimeZone timeZone){
        this();
        this.setCalendar(Calendar.getInstance(timeZone));
    }
    public StringBuffer format(final Date date,final StringBuffer sbuf,final FieldPosition fieldPosition){
        super.calendar.setTime(date);
        final int day=super.calendar.get(5);
        if(day<10){
            sbuf.append('0');
        }
        sbuf.append(day);
        sbuf.append(' ');
        sbuf.append(this.shortMonths[super.calendar.get(2)]);
        sbuf.append(' ');
        final int year=super.calendar.get(1);
        sbuf.append(year);
        sbuf.append(' ');
        return super.format(date,sbuf,fieldPosition);
    }
    public Date parse(final String s,final ParsePosition pos){
        return null;
    }
}
