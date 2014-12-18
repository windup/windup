package org.apache.log4j.lf5.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatManager{
    private TimeZone _timeZone;
    private Locale _locale;
    private String _pattern;
    private DateFormat _dateFormat;
    public DateFormatManager(){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this.configure();
    }
    public DateFormatManager(final TimeZone timeZone){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this._timeZone=timeZone;
        this.configure();
    }
    public DateFormatManager(final Locale locale){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this._locale=locale;
        this.configure();
    }
    public DateFormatManager(final String pattern){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this._pattern=pattern;
        this.configure();
    }
    public DateFormatManager(final TimeZone timeZone,final Locale locale){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this._timeZone=timeZone;
        this._locale=locale;
        this.configure();
    }
    public DateFormatManager(final TimeZone timeZone,final String pattern){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this._timeZone=timeZone;
        this._pattern=pattern;
        this.configure();
    }
    public DateFormatManager(final Locale locale,final String pattern){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this._locale=locale;
        this._pattern=pattern;
        this.configure();
    }
    public DateFormatManager(final TimeZone timeZone,final Locale locale,final String pattern){
        super();
        this._timeZone=null;
        this._locale=null;
        this._pattern=null;
        this._dateFormat=null;
        this._timeZone=timeZone;
        this._locale=locale;
        this._pattern=pattern;
        this.configure();
    }
    public synchronized TimeZone getTimeZone(){
        if(this._timeZone==null){
            return TimeZone.getDefault();
        }
        return this._timeZone;
    }
    public synchronized void setTimeZone(TimeZone timeZone){
        timeZone=timeZone;
        this.configure();
    }
    public synchronized Locale getLocale(){
        if(this._locale==null){
            return Locale.getDefault();
        }
        return this._locale;
    }
    public synchronized void setLocale(final Locale locale){
        this._locale=locale;
        this.configure();
    }
    public synchronized String getPattern(){
        return this._pattern;
    }
    public synchronized void setPattern(final String pattern){
        this._pattern=pattern;
        this.configure();
    }
    public synchronized String getOutputFormat(){
        return this._pattern;
    }
    public synchronized void setOutputFormat(final String pattern){
        this._pattern=pattern;
        this.configure();
    }
    public synchronized DateFormat getDateFormatInstance(){
        return this._dateFormat;
    }
    public synchronized void setDateFormatInstance(final DateFormat dateFormat){
        this._dateFormat=dateFormat;
    }
    public String format(final Date date){
        return this.getDateFormatInstance().format(date);
    }
    public String format(final Date date,final String pattern){
        DateFormat formatter=null;
        formatter=this.getDateFormatInstance();
        if(formatter instanceof SimpleDateFormat){
            formatter=(SimpleDateFormat)formatter.clone();
            ((SimpleDateFormat)formatter).applyPattern(pattern);
        }
        return formatter.format(date);
    }
    public Date parse(final String date) throws ParseException{
        return this.getDateFormatInstance().parse(date);
    }
    public Date parse(final String date,final String pattern) throws ParseException{
        DateFormat formatter=null;
        formatter=this.getDateFormatInstance();
        if(formatter instanceof SimpleDateFormat){
            formatter=(SimpleDateFormat)formatter.clone();
            ((SimpleDateFormat)formatter).applyPattern(pattern);
        }
        return formatter.parse(date);
    }
    private synchronized void configure(){
        (this._dateFormat=DateFormat.getDateTimeInstance(0,0,this.getLocale())).setTimeZone(this.getTimeZone());
        if(this._pattern!=null){
            ((SimpleDateFormat)this._dateFormat).applyPattern(this._pattern);
        }
    }
}
