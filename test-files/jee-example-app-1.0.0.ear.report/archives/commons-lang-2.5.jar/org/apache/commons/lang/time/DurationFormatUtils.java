package org.apache.commons.lang.time;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;

public class DurationFormatUtils{
    public static final String ISO_EXTENDED_FORMAT_PATTERN="'P'yyyy'Y'M'M'd'DT'H'H'm'M's.S'S'";
    static final Object y;
    static final Object M;
    static final Object d;
    static final Object H;
    static final Object m;
    static final Object s;
    static final Object S;
    public static String formatDurationHMS(final long durationMillis){
        return formatDuration(durationMillis,"H:mm:ss.SSS");
    }
    public static String formatDurationISO(final long durationMillis){
        return formatDuration(durationMillis,"'P'yyyy'Y'M'M'd'DT'H'H'm'M's.S'S'",false);
    }
    public static String formatDuration(final long durationMillis,final String format){
        return formatDuration(durationMillis,format,true);
    }
    public static String formatDuration(long durationMillis,final String format,final boolean padWithZeros){
        final Token[] tokens=lexx(format);
        int days=0;
        int hours=0;
        int minutes=0;
        int seconds=0;
        int milliseconds=0;
        if(Token.containsTokenWithValue(tokens,DurationFormatUtils.d)){
            days=(int)(durationMillis/86400000L);
            durationMillis-=days*86400000L;
        }
        if(Token.containsTokenWithValue(tokens,DurationFormatUtils.H)){
            hours=(int)(durationMillis/3600000L);
            durationMillis-=hours*3600000L;
        }
        if(Token.containsTokenWithValue(tokens,DurationFormatUtils.m)){
            minutes=(int)(durationMillis/60000L);
            durationMillis-=minutes*60000L;
        }
        if(Token.containsTokenWithValue(tokens,DurationFormatUtils.s)){
            seconds=(int)(durationMillis/1000L);
            durationMillis-=seconds*1000L;
        }
        if(Token.containsTokenWithValue(tokens,DurationFormatUtils.S)){
            milliseconds=(int)durationMillis;
        }
        return format(tokens,0,0,days,hours,minutes,seconds,milliseconds,padWithZeros);
    }
    public static String formatDurationWords(final long durationMillis,final boolean suppressLeadingZeroElements,final boolean suppressTrailingZeroElements){
        String duration=formatDuration(durationMillis,"d' days 'H' hours 'm' minutes 's' seconds'");
        if(suppressLeadingZeroElements){
            duration=" "+duration;
            String tmp=StringUtils.replaceOnce(duration," 0 days","");
            if(tmp.length()!=duration.length()){
                duration=tmp;
                tmp=StringUtils.replaceOnce(duration," 0 hours","");
                if(tmp.length()!=duration.length()){
                    duration=tmp;
                    tmp=(duration=StringUtils.replaceOnce(duration," 0 minutes",""));
                    if(tmp.length()!=duration.length()){
                        duration=StringUtils.replaceOnce(tmp," 0 seconds","");
                    }
                }
            }
            if(duration.length()!=0){
                duration=duration.substring(1);
            }
        }
        if(suppressTrailingZeroElements){
            String tmp=StringUtils.replaceOnce(duration," 0 seconds","");
            if(tmp.length()!=duration.length()){
                duration=tmp;
                tmp=StringUtils.replaceOnce(duration," 0 minutes","");
                if(tmp.length()!=duration.length()){
                    duration=tmp;
                    tmp=StringUtils.replaceOnce(duration," 0 hours","");
                    if(tmp.length()!=duration.length()){
                        duration=StringUtils.replaceOnce(tmp," 0 days","");
                    }
                }
            }
        }
        duration=" "+duration;
        duration=StringUtils.replaceOnce(duration," 1 seconds"," 1 second");
        duration=StringUtils.replaceOnce(duration," 1 minutes"," 1 minute");
        duration=StringUtils.replaceOnce(duration," 1 hours"," 1 hour");
        duration=StringUtils.replaceOnce(duration," 1 days"," 1 day");
        return duration.trim();
    }
    public static String formatPeriodISO(final long startMillis,final long endMillis){
        return formatPeriod(startMillis,endMillis,"'P'yyyy'Y'M'M'd'DT'H'H'm'M's.S'S'",false,TimeZone.getDefault());
    }
    public static String formatPeriod(final long startMillis,final long endMillis,final String format){
        return formatPeriod(startMillis,endMillis,format,true,TimeZone.getDefault());
    }
    public static String formatPeriod(final long startMillis,final long endMillis,final String format,final boolean padWithZeros,final TimeZone timezone){
        final Token[] tokens=lexx(format);
        final Calendar start=Calendar.getInstance(timezone);
        start.setTime(new Date(startMillis));
        final Calendar end=Calendar.getInstance(timezone);
        end.setTime(new Date(endMillis));
        int milliseconds=end.get(14)-start.get(14);
        int seconds=end.get(13)-start.get(13);
        int minutes=end.get(12)-start.get(12);
        int hours=end.get(11)-start.get(11);
        int days=end.get(5)-start.get(5);
        int months=end.get(2)-start.get(2);
        int years=end.get(1)-start.get(1);
        while(milliseconds<0){
            milliseconds+=1000;
            --seconds;
        }
        while(seconds<0){
            seconds+=60;
            --minutes;
        }
        while(minutes<0){
            minutes+=60;
            --hours;
        }
        while(hours<0){
            hours+=24;
            --days;
        }
        if(Token.containsTokenWithValue(tokens,DurationFormatUtils.M)){
            while(days<0){
                days+=start.getActualMaximum(5);
                --months;
                start.add(2,1);
            }
            while(months<0){
                months+=12;
                --years;
            }
            if(!Token.containsTokenWithValue(tokens,DurationFormatUtils.y)&&years!=0){
                while(years!=0){
                    months+=12*years;
                    years=0;
                }
            }
        }
        else{
            if(!Token.containsTokenWithValue(tokens,DurationFormatUtils.y)){
                int target=end.get(1);
                if(months<0){
                    --target;
                }
                while(start.get(1)!=target){
                    days+=start.getActualMaximum(6)-start.get(6);
                    if(start instanceof GregorianCalendar&&start.get(2)==1&&start.get(5)==29){
                        ++days;
                    }
                    start.add(1,1);
                    days+=start.get(6);
                }
                years=0;
            }
            while(start.get(2)!=end.get(2)){
                days+=start.getActualMaximum(5);
                start.add(2,1);
            }
            months=0;
            while(days<0){
                days+=start.getActualMaximum(5);
                --months;
                start.add(2,1);
            }
        }
        if(!Token.containsTokenWithValue(tokens,DurationFormatUtils.d)){
            hours+=24*days;
            days=0;
        }
        if(!Token.containsTokenWithValue(tokens,DurationFormatUtils.H)){
            minutes+=60*hours;
            hours=0;
        }
        if(!Token.containsTokenWithValue(tokens,DurationFormatUtils.m)){
            seconds+=60*minutes;
            minutes=0;
        }
        if(!Token.containsTokenWithValue(tokens,DurationFormatUtils.s)){
            milliseconds+=1000*seconds;
            seconds=0;
        }
        return format(tokens,years,months,days,hours,minutes,seconds,milliseconds,padWithZeros);
    }
    static String format(final Token[] tokens,final int years,final int months,final int days,final int hours,final int minutes,final int seconds,int milliseconds,final boolean padWithZeros){
        final StringBuffer buffer=new StringBuffer();
        boolean lastOutputSeconds=false;
        for(final Token token : tokens){
            final Object value=token.getValue();
            final int count=token.getCount();
            if(value instanceof StringBuffer){
                buffer.append(value.toString());
            }
            else if(value==DurationFormatUtils.y){
                buffer.append(padWithZeros?StringUtils.leftPad(Integer.toString(years),count,'0'):Integer.toString(years));
                lastOutputSeconds=false;
            }
            else if(value==DurationFormatUtils.M){
                buffer.append(padWithZeros?StringUtils.leftPad(Integer.toString(months),count,'0'):Integer.toString(months));
                lastOutputSeconds=false;
            }
            else if(value==DurationFormatUtils.d){
                buffer.append(padWithZeros?StringUtils.leftPad(Integer.toString(days),count,'0'):Integer.toString(days));
                lastOutputSeconds=false;
            }
            else if(value==DurationFormatUtils.H){
                buffer.append(padWithZeros?StringUtils.leftPad(Integer.toString(hours),count,'0'):Integer.toString(hours));
                lastOutputSeconds=false;
            }
            else if(value==DurationFormatUtils.m){
                buffer.append(padWithZeros?StringUtils.leftPad(Integer.toString(minutes),count,'0'):Integer.toString(minutes));
                lastOutputSeconds=false;
            }
            else if(value==DurationFormatUtils.s){
                buffer.append(padWithZeros?StringUtils.leftPad(Integer.toString(seconds),count,'0'):Integer.toString(seconds));
                lastOutputSeconds=true;
            }
            else if(value==DurationFormatUtils.S){
                if(lastOutputSeconds){
                    milliseconds+=1000;
                    final String str=padWithZeros?StringUtils.leftPad(Integer.toString(milliseconds),count,'0'):Integer.toString(milliseconds);
                    buffer.append(str.substring(1));
                }
                else{
                    buffer.append(padWithZeros?StringUtils.leftPad(Integer.toString(milliseconds),count,'0'):Integer.toString(milliseconds));
                }
                lastOutputSeconds=false;
            }
        }
        return buffer.toString();
    }
    static Token[] lexx(final String format){
        final char[] array=format.toCharArray();
        final ArrayList list=new ArrayList(array.length);
        boolean inLiteral=false;
        StringBuffer buffer=null;
        Token previous=null;
        for(final char ch : array){
            if(inLiteral&&ch!='\''){
                buffer.append(ch);
            }
            else{
                Object value=null;
                switch(ch){
                    case '\'':{
                        if(inLiteral){
                            buffer=null;
                            inLiteral=false;
                            break;
                        }
                        buffer=new StringBuffer();
                        list.add(new Token(buffer));
                        inLiteral=true;
                        break;
                    }
                    case 'y':{
                        value=DurationFormatUtils.y;
                        break;
                    }
                    case 'M':{
                        value=DurationFormatUtils.M;
                        break;
                    }
                    case 'd':{
                        value=DurationFormatUtils.d;
                        break;
                    }
                    case 'H':{
                        value=DurationFormatUtils.H;
                        break;
                    }
                    case 'm':{
                        value=DurationFormatUtils.m;
                        break;
                    }
                    case 's':{
                        value=DurationFormatUtils.s;
                        break;
                    }
                    case 'S':{
                        value=DurationFormatUtils.S;
                        break;
                    }
                    default:{
                        if(buffer==null){
                            buffer=new StringBuffer();
                            list.add(new Token(buffer));
                        }
                        buffer.append(ch);
                        break;
                    }
                }
                if(value!=null){
                    if(previous!=null&&previous.getValue()==value){
                        previous.increment();
                    }
                    else{
                        final Token token=new Token(value);
                        list.add(token);
                        previous=token;
                    }
                    buffer=null;
                }
            }
        }
        return list.toArray(new Token[list.size()]);
    }
    static{
        y="y";
        M="M";
        d="d";
        H="H";
        m="m";
        s="s";
        S="S";
    }
    static class Token{
        private Object value;
        private int count;
        static boolean containsTokenWithValue(final Token[] tokens,final Object value){
            for(int sz=tokens.length,i=0;i<sz;++i){
                if(tokens[i].getValue()==value){
                    return true;
                }
            }
            return false;
        }
        Token(final Object value){
            super();
            this.value=value;
            this.count=1;
        }
        Token(final Object value,final int count){
            super();
            this.value=value;
            this.count=count;
        }
        void increment(){
            ++this.count;
        }
        int getCount(){
            return this.count;
        }
        Object getValue(){
            return this.value;
        }
        public boolean equals(final Object obj2){
            if(!(obj2 instanceof Token)){
                return false;
            }
            final Token tok2=(Token)obj2;
            if(this.value.getClass()!=tok2.value.getClass()){
                return false;
            }
            if(this.count!=tok2.count){
                return false;
            }
            if(this.value instanceof StringBuffer){
                return this.value.toString().equals(tok2.value.toString());
            }
            if(this.value instanceof Number){
                return this.value.equals(tok2.value);
            }
            return this.value==tok2.value;
        }
        public int hashCode(){
            return this.value.hashCode();
        }
        public String toString(){
            return StringUtils.repeat(this.value.toString(),this.count);
        }
    }
}
