package org.apache.commons.lang.time;

import java.util.NoSuchElementException;
import java.util.Iterator;
import org.apache.commons.lang.StringUtils;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils{
    public static final TimeZone UTC_TIME_ZONE;
    public static final long MILLIS_PER_SECOND=1000L;
    public static final long MILLIS_PER_MINUTE=60000L;
    public static final long MILLIS_PER_HOUR=3600000L;
    public static final long MILLIS_PER_DAY=86400000L;
    public static final int SEMI_MONTH=1001;
    private static final int[][] fields;
    public static final int RANGE_WEEK_SUNDAY=1;
    public static final int RANGE_WEEK_MONDAY=2;
    public static final int RANGE_WEEK_RELATIVE=3;
    public static final int RANGE_WEEK_CENTER=4;
    public static final int RANGE_MONTH_SUNDAY=5;
    public static final int RANGE_MONTH_MONDAY=6;
    private static final int MODIFY_TRUNCATE=0;
    private static final int MODIFY_ROUND=1;
    private static final int MODIFY_CEILING=2;
    public static final int MILLIS_IN_SECOND=1000;
    public static final int MILLIS_IN_MINUTE=60000;
    public static final int MILLIS_IN_HOUR=3600000;
    public static final int MILLIS_IN_DAY=86400000;
    public static boolean isSameDay(final Date date1,final Date date2){
        if(date1==null||date2==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar cal1=Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2=Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1,cal2);
    }
    public static boolean isSameDay(final Calendar cal1,final Calendar cal2){
        if(cal1==null||cal2==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(0)==cal2.get(0)&&cal1.get(1)==cal2.get(1)&&cal1.get(6)==cal2.get(6);
    }
    public static boolean isSameInstant(final Date date1,final Date date2){
        if(date1==null||date2==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        return date1.getTime()==date2.getTime();
    }
    public static boolean isSameInstant(final Calendar cal1,final Calendar cal2){
        if(cal1==null||cal2==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.getTime().getTime()==cal2.getTime().getTime();
    }
    public static boolean isSameLocalTime(final Calendar cal1,final Calendar cal2){
        if(cal1==null||cal2==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(14)==cal2.get(14)&&cal1.get(13)==cal2.get(13)&&cal1.get(12)==cal2.get(12)&&cal1.get(10)==cal2.get(10)&&cal1.get(6)==cal2.get(6)&&cal1.get(1)==cal2.get(1)&&cal1.get(0)==cal2.get(0)&&cal1.getClass()==cal2.getClass();
    }
    public static Date parseDate(final String str,final String[] parsePatterns) throws ParseException{
        return parseDateWithLeniency(str,parsePatterns,true);
    }
    public static Date parseDateStrictly(final String str,final String[] parsePatterns) throws ParseException{
        return parseDateWithLeniency(str,parsePatterns,false);
    }
    private static Date parseDateWithLeniency(final String str,final String[] parsePatterns,final boolean lenient) throws ParseException{
        if(str==null||parsePatterns==null){
            throw new IllegalArgumentException("Date and Patterns must not be null");
        }
        final SimpleDateFormat parser=new SimpleDateFormat();
        parser.setLenient(lenient);
        final ParsePosition pos=new ParsePosition(0);
        for(int i=0;i<parsePatterns.length;++i){
            String pattern=parsePatterns[i];
            if(parsePatterns[i].endsWith("ZZ")){
                pattern=pattern.substring(0,pattern.length()-1);
            }
            parser.applyPattern(pattern);
            pos.setIndex(0);
            String str2=str;
            if(parsePatterns[i].endsWith("ZZ")){
                for(int signIdx=indexOfSignChars(str2,0);signIdx>=0;signIdx=indexOfSignChars(str2,++signIdx)){
                    str2=reformatTimezone(str2,signIdx);
                }
            }
            final Date date=parser.parse(str2,pos);
            if(date!=null&&pos.getIndex()==str2.length()){
                return date;
            }
        }
        throw new ParseException("Unable to parse the date: "+str,-1);
    }
    private static int indexOfSignChars(final String str,final int startPos){
        int idx=StringUtils.indexOf(str,'+',startPos);
        if(idx<0){
            idx=StringUtils.indexOf(str,'-',startPos);
        }
        return idx;
    }
    private static String reformatTimezone(final String str,final int signIdx){
        String str2=str;
        if(signIdx>=0&&signIdx+5<str.length()&&Character.isDigit(str.charAt(signIdx+1))&&Character.isDigit(str.charAt(signIdx+2))&&str.charAt(signIdx+3)==':'&&Character.isDigit(str.charAt(signIdx+4))&&Character.isDigit(str.charAt(signIdx+5))){
            str2=str.substring(0,signIdx+3)+str.substring(signIdx+4);
        }
        return str2;
    }
    public static Date addYears(final Date date,final int amount){
        return add(date,1,amount);
    }
    public static Date addMonths(final Date date,final int amount){
        return add(date,2,amount);
    }
    public static Date addWeeks(final Date date,final int amount){
        return add(date,3,amount);
    }
    public static Date addDays(final Date date,final int amount){
        return add(date,5,amount);
    }
    public static Date addHours(final Date date,final int amount){
        return add(date,11,amount);
    }
    public static Date addMinutes(final Date date,final int amount){
        return add(date,12,amount);
    }
    public static Date addSeconds(final Date date,final int amount){
        return add(date,13,amount);
    }
    public static Date addMilliseconds(final Date date,final int amount){
        return add(date,14,amount);
    }
    public static Date add(final Date date,final int calendarField,final int amount){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar c=Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField,amount);
        return c.getTime();
    }
    public static Date setYears(final Date date,final int amount){
        return set(date,1,amount);
    }
    public static Date setMonths(final Date date,final int amount){
        return set(date,2,amount);
    }
    public static Date setDays(final Date date,final int amount){
        return set(date,5,amount);
    }
    public static Date setHours(final Date date,final int amount){
        return set(date,11,amount);
    }
    public static Date setMinutes(final Date date,final int amount){
        return set(date,12,amount);
    }
    public static Date setSeconds(final Date date,final int amount){
        return set(date,13,amount);
    }
    public static Date setMilliseconds(final Date date,final int amount){
        return set(date,14,amount);
    }
    private static Date set(final Date date,final int calendarField,final int amount){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar c=Calendar.getInstance();
        c.setLenient(false);
        c.setTime(date);
        c.set(calendarField,amount);
        return c.getTime();
    }
    public static Date round(final Date date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar gval=Calendar.getInstance();
        gval.setTime(date);
        modify(gval,field,1);
        return gval.getTime();
    }
    public static Calendar round(final Calendar date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar rounded=(Calendar)date.clone();
        modify(rounded,field,1);
        return rounded;
    }
    public static Date round(final Object date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        if(date instanceof Date){
            return round((Date)date,field);
        }
        if(date instanceof Calendar){
            return round((Calendar)date,field).getTime();
        }
        throw new ClassCastException("Could not round "+date);
    }
    public static Date truncate(final Date date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar gval=Calendar.getInstance();
        gval.setTime(date);
        modify(gval,field,0);
        return gval.getTime();
    }
    public static Calendar truncate(final Calendar date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar truncated=(Calendar)date.clone();
        modify(truncated,field,0);
        return truncated;
    }
    public static Date truncate(final Object date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        if(date instanceof Date){
            return truncate((Date)date,field);
        }
        if(date instanceof Calendar){
            return truncate((Calendar)date,field).getTime();
        }
        throw new ClassCastException("Could not truncate "+date);
    }
    public static Date ceiling(final Date date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar gval=Calendar.getInstance();
        gval.setTime(date);
        modify(gval,field,2);
        return gval.getTime();
    }
    public static Calendar ceiling(final Calendar date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar ceiled=(Calendar)date.clone();
        modify(ceiled,field,2);
        return ceiled;
    }
    public static Date ceiling(final Object date,final int field){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        if(date instanceof Date){
            return ceiling((Date)date,field);
        }
        if(date instanceof Calendar){
            return ceiling((Calendar)date,field).getTime();
        }
        throw new ClassCastException("Could not find ceiling of for type: "+date.getClass());
    }
    private static void modify(final Calendar val,final int field,final int modType){
        if(val.get(1)>280000000){
            throw new ArithmeticException("Calendar value too large for accurate calculations");
        }
        if(field==14){
            return;
        }
        final Date date=val.getTime();
        long time=date.getTime();
        boolean done=false;
        final int millisecs=val.get(14);
        if(0==modType||millisecs<500){
            time-=millisecs;
        }
        if(field==13){
            done=true;
        }
        final int seconds=val.get(13);
        if(!done&&(0==modType||seconds<30)){
            time-=seconds*1000L;
        }
        if(field==12){
            done=true;
        }
        final int minutes=val.get(12);
        if(!done&&(0==modType||minutes<30)){
            time-=minutes*60000L;
        }
        if(date.getTime()!=time){
            date.setTime(time);
            val.setTime(date);
        }
        boolean roundUp=false;
        for(int i=0;i<DateUtils.fields.length;++i){
            for(int j=0;j<DateUtils.fields[i].length;++j){
                if(DateUtils.fields[i][j]==field){
                    if(modType==2||(modType==1&&roundUp)){
                        if(field==1001){
                            if(val.get(5)==1){
                                val.add(5,15);
                            }
                            else{
                                val.add(5,-15);
                                val.add(2,1);
                            }
                        }
                        else if(field==9){
                            if(val.get(11)==0){
                                val.add(11,12);
                            }
                            else{
                                val.add(11,-12);
                                val.add(5,1);
                            }
                        }
                        else{
                            val.add(DateUtils.fields[i][0],1);
                        }
                    }
                    return;
                }
            }
            int offset=0;
            boolean offsetSet=false;
            switch(field){
                case 1001:{
                    if(DateUtils.fields[i][0]==5){
                        offset=val.get(5)-1;
                        if(offset>=15){
                            offset-=15;
                        }
                        roundUp=(offset>7);
                        offsetSet=true;
                        break;
                    }
                    break;
                }
                case 9:{
                    if(DateUtils.fields[i][0]==11){
                        offset=val.get(11);
                        if(offset>=12){
                            offset-=12;
                        }
                        roundUp=(offset>=6);
                        offsetSet=true;
                        break;
                    }
                    break;
                }
            }
            if(!offsetSet){
                final int min=val.getActualMinimum(DateUtils.fields[i][0]);
                final int max=val.getActualMaximum(DateUtils.fields[i][0]);
                offset=val.get(DateUtils.fields[i][0])-min;
                roundUp=(offset>(max-min)/2);
            }
            if(offset!=0){
                val.set(DateUtils.fields[i][0],val.get(DateUtils.fields[i][0])-offset);
            }
        }
        throw new IllegalArgumentException("The field "+field+" is not supported");
    }
    public static Iterator iterator(final Date focus,final int rangeStyle){
        if(focus==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar gval=Calendar.getInstance();
        gval.setTime(focus);
        return iterator(gval,rangeStyle);
    }
    public static Iterator iterator(final Calendar focus,final int rangeStyle){
        if(focus==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar start=null;
        Calendar end=null;
        int startCutoff=1;
        int endCutoff=7;
        switch(rangeStyle){
            case 5:
            case 6:{
                start=truncate(focus,2);
                end=(Calendar)start.clone();
                end.add(2,1);
                end.add(5,-1);
                if(rangeStyle==6){
                    startCutoff=2;
                    endCutoff=1;
                    break;
                }
                break;
            }
            case 1:
            case 2:
            case 3:
            case 4:{
                start=truncate(focus,5);
                end=truncate(focus,5);
                switch(rangeStyle){
                    case 2:{
                        startCutoff=2;
                        endCutoff=1;
                        break;
                    }
                    case 3:{
                        startCutoff=focus.get(7);
                        endCutoff=startCutoff-1;
                        break;
                    }
                    case 4:{
                        startCutoff=focus.get(7)-3;
                        endCutoff=focus.get(7)+3;
                        break;
                    }
                }
                break;
            }
            default:{
                throw new IllegalArgumentException("The range style "+rangeStyle+" is not valid.");
            }
        }
        if(startCutoff<1){
            startCutoff+=7;
        }
        if(startCutoff>7){
            startCutoff-=7;
        }
        if(endCutoff<1){
            endCutoff+=7;
        }
        if(endCutoff>7){
            endCutoff-=7;
        }
        while(start.get(7)!=startCutoff){
            start.add(5,-1);
        }
        while(end.get(7)!=endCutoff){
            end.add(5,1);
        }
        return new DateIterator(start,end);
    }
    public static Iterator iterator(final Object focus,final int rangeStyle){
        if(focus==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        if(focus instanceof Date){
            return iterator((Date)focus,rangeStyle);
        }
        if(focus instanceof Calendar){
            return iterator((Calendar)focus,rangeStyle);
        }
        throw new ClassCastException("Could not iterate based on "+focus);
    }
    public static long getFragmentInMilliseconds(final Date date,final int fragment){
        return getFragment(date,fragment,14);
    }
    public static long getFragmentInSeconds(final Date date,final int fragment){
        return getFragment(date,fragment,13);
    }
    public static long getFragmentInMinutes(final Date date,final int fragment){
        return getFragment(date,fragment,12);
    }
    public static long getFragmentInHours(final Date date,final int fragment){
        return getFragment(date,fragment,11);
    }
    public static long getFragmentInDays(final Date date,final int fragment){
        return getFragment(date,fragment,6);
    }
    public static long getFragmentInMilliseconds(final Calendar calendar,final int fragment){
        return getFragment(calendar,fragment,14);
    }
    public static long getFragmentInSeconds(final Calendar calendar,final int fragment){
        return getFragment(calendar,fragment,13);
    }
    public static long getFragmentInMinutes(final Calendar calendar,final int fragment){
        return getFragment(calendar,fragment,12);
    }
    public static long getFragmentInHours(final Calendar calendar,final int fragment){
        return getFragment(calendar,fragment,11);
    }
    public static long getFragmentInDays(final Calendar calendar,final int fragment){
        return getFragment(calendar,fragment,6);
    }
    private static long getFragment(final Date date,final int fragment,final int unit){
        if(date==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        return getFragment(calendar,fragment,unit);
    }
    private static long getFragment(final Calendar calendar,final int fragment,final int unit){
        if(calendar==null){
            throw new IllegalArgumentException("The date must not be null");
        }
        final long millisPerUnit=getMillisPerUnit(unit);
        long result=0L;
        switch(fragment){
            case 1:{
                result+=calendar.get(6)*86400000L/millisPerUnit;
                break;
            }
            case 2:{
                result+=calendar.get(5)*86400000L/millisPerUnit;
                break;
            }
        }
        switch(fragment){
            case 1:
            case 2:
            case 5:
            case 6:{
                result+=calendar.get(11)*3600000L/millisPerUnit;
            }
            case 11:{
                result+=calendar.get(12)*60000L/millisPerUnit;
            }
            case 12:{
                result+=calendar.get(13)*1000L/millisPerUnit;
            }
            case 13:{
                result+=calendar.get(14)*1/millisPerUnit;
                break;
            }
            case 14:{
                break;
            }
            default:{
                throw new IllegalArgumentException("The fragment "+fragment+" is not supported");
            }
        }
        return result;
    }
    private static long getMillisPerUnit(final int unit){
        long result=Long.MAX_VALUE;
        switch(unit){
            case 5:
            case 6:{
                result=86400000L;
                break;
            }
            case 11:{
                result=3600000L;
                break;
            }
            case 12:{
                result=60000L;
                break;
            }
            case 13:{
                result=1000L;
                break;
            }
            case 14:{
                result=1L;
                break;
            }
            default:{
                throw new IllegalArgumentException("The unit "+unit+" cannot be represented is milleseconds");
            }
        }
        return result;
    }
    static{
        UTC_TIME_ZONE=TimeZone.getTimeZone("GMT");
        fields=new int[][] { { 14 }, { 13 }, { 12 }, { 11,10 }, { 5,5,9 }, { 2,1001 }, { 1 }, { 0 } };
    }
    static class DateIterator implements Iterator{
        private final Calendar endFinal;
        private final Calendar spot;
        DateIterator(final Calendar startFinal,final Calendar endFinal){
            super();
            this.endFinal=endFinal;
            (this.spot=startFinal).add(5,-1);
        }
        public boolean hasNext(){
            return this.spot.before(this.endFinal);
        }
        public Object next(){
            if(this.spot.equals(this.endFinal)){
                throw new NoSuchElementException();
            }
            this.spot.add(5,1);
            return this.spot.clone();
        }
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }
}
