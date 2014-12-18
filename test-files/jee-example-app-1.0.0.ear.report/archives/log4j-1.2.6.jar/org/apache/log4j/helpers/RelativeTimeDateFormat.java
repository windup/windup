package org.apache.log4j.helpers;

import java.text.ParsePosition;
import java.text.FieldPosition;
import java.util.Date;
import java.text.DateFormat;

public class RelativeTimeDateFormat extends DateFormat{
    protected final long startTime;
    public RelativeTimeDateFormat(){
        super();
        this.startTime=System.currentTimeMillis();
    }
    public StringBuffer format(final Date date,final StringBuffer sbuf,final FieldPosition fieldPosition){
        return sbuf.append(date.getTime()-this.startTime);
    }
    public Date parse(final String s,final ParsePosition pos){
        return null;
    }
}
