package org.apache.log4j.helpers;

import org.apache.log4j.spi.LocationInfo;
import java.util.Date;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.OptionConverter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.log4j.helpers.DateTimeDateFormat;
import org.apache.log4j.helpers.AbsoluteTimeDateFormat;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.FormattingInfo;
import org.apache.log4j.helpers.PatternConverter;

public class PatternParser{
    private static final char ESCAPE_CHAR='%';
    private static final int LITERAL_STATE=0;
    private static final int CONVERTER_STATE=1;
    private static final int MINUS_STATE=2;
    private static final int DOT_STATE=3;
    private static final int MIN_STATE=4;
    private static final int MAX_STATE=5;
    static final int FULL_LOCATION_CONVERTER=1000;
    static final int METHOD_LOCATION_CONVERTER=1001;
    static final int CLASS_LOCATION_CONVERTER=1002;
    static final int LINE_LOCATION_CONVERTER=1003;
    static final int FILE_LOCATION_CONVERTER=1004;
    static final int RELATIVE_TIME_CONVERTER=2000;
    static final int THREAD_CONVERTER=2001;
    static final int LEVEL_CONVERTER=2002;
    static final int NDC_CONVERTER=2003;
    static final int MESSAGE_CONVERTER=2004;
    int state;
    protected StringBuffer currentLiteral;
    protected int patternLength;
    protected int i;
    PatternConverter head;
    PatternConverter tail;
    protected FormattingInfo formattingInfo;
    protected String pattern;
    static /* synthetic */ Class class$java$text$DateFormat;
    public PatternParser(final String pattern){
        super();
        this.currentLiteral=new StringBuffer(32);
        this.formattingInfo=new FormattingInfo();
        this.pattern=pattern;
        this.patternLength=pattern.length();
        this.state=0;
    }
    private void addToList(final PatternConverter pc){
        if(this.head==null){
            this.tail=pc;
            this.head=pc;
        }
        else{
            this.tail.next=pc;
            this.tail=pc;
        }
    }
    protected String extractOption(){
        if(this.i<this.patternLength&&this.pattern.charAt(this.i)=='{'){
            final int end=this.pattern.indexOf(125,this.i);
            if(end>this.i){
                final String r=this.pattern.substring(this.i+1,end);
                this.i=end+1;
                return r;
            }
        }
        return null;
    }
    protected int extractPrecisionOption(){
        final String opt=this.extractOption();
        int r=0;
        if(opt!=null){
            try{
                r=Integer.parseInt(opt);
                if(r<=0){
                    LogLog.error("Precision option ("+opt+") isn't a positive integer.");
                    r=0;
                }
            }
            catch(NumberFormatException e){
                LogLog.error("Category option \""+opt+"\" not a decimal integer.",e);
            }
        }
        return r;
    }
    public PatternConverter parse(){
        this.i=0;
        while(this.i<this.patternLength){
            final char c=this.pattern.charAt(this.i++);
            switch(this.state){
                case 0:{
                    if(this.i==this.patternLength){
                        this.currentLiteral.append(c);
                        continue;
                    }
                    if(c!='%'){
                        this.currentLiteral.append(c);
                        continue;
                    }
                    switch(this.pattern.charAt(this.i)){
                        case '%':{
                            this.currentLiteral.append(c);
                            ++this.i;
                            continue;
                        }
                        case 'n':{
                            this.currentLiteral.append(Layout.LINE_SEP);
                            ++this.i;
                            continue;
                        }
                        default:{
                            if(this.currentLiteral.length()!=0){
                                this.addToList(new LiteralPatternConverter(this.currentLiteral.toString()));
                            }
                            this.currentLiteral.setLength(0);
                            this.currentLiteral.append(c);
                            this.state=1;
                            this.formattingInfo.reset();
                            continue;
                        }
                    }
                    break;
                }
                case 1:{
                    this.currentLiteral.append(c);
                    switch(c){
                        case '-':{
                            this.formattingInfo.leftAlign=true;
                            continue;
                        }
                        case '.':{
                            this.state=3;
                            continue;
                        }
                        default:{
                            if(c>='0'&&c<='9'){
                                this.formattingInfo.min=c-'0';
                                this.state=4;
                                continue;
                            }
                            this.finalizeConverter(c);
                            continue;
                        }
                    }
                    break;
                }
                case 4:{
                    this.currentLiteral.append(c);
                    if(c>='0'&&c<='9'){
                        this.formattingInfo.min=this.formattingInfo.min*10+(c-'0');
                        continue;
                    }
                    if(c=='.'){
                        this.state=3;
                        continue;
                    }
                    this.finalizeConverter(c);
                    continue;
                }
                case 3:{
                    this.currentLiteral.append(c);
                    if(c>='0'&&c<='9'){
                        this.formattingInfo.max=c-'0';
                        this.state=5;
                        continue;
                    }
                    LogLog.error("Error occured in position "+this.i+".\n Was expecting digit, instead got char \""+c+"\".");
                    this.state=0;
                    continue;
                }
                case 5:{
                    this.currentLiteral.append(c);
                    if(c>='0'&&c<='9'){
                        this.formattingInfo.max=this.formattingInfo.max*10+(c-'0');
                        continue;
                    }
                    this.finalizeConverter(c);
                    this.state=0;
                    continue;
                }
                default:{
                    continue;
                }
            }
        }
        if(this.currentLiteral.length()!=0){
            this.addToList(new LiteralPatternConverter(this.currentLiteral.toString()));
        }
        return this.head;
    }
    protected void finalizeConverter(final char c){
        PatternConverter pc=null;
        switch(c){
            case 'c':{
                pc=new CategoryPatternConverter(this.formattingInfo,this.extractPrecisionOption());
                this.currentLiteral.setLength(0);
                break;
            }
            case 'C':{
                pc=new ClassNamePatternConverter(this.formattingInfo,this.extractPrecisionOption());
                this.currentLiteral.setLength(0);
                break;
            }
            case 'd':{
                String dateFormatStr="ISO8601";
                final String dOpt=this.extractOption();
                if(dOpt!=null){
                    dateFormatStr=dOpt;
                }
                DateFormat df;
                if(dateFormatStr.equalsIgnoreCase("ISO8601")){
                    df=new ISO8601DateFormat();
                }
                else if(dateFormatStr.equalsIgnoreCase("ABSOLUTE")){
                    df=new AbsoluteTimeDateFormat();
                }
                else if(dateFormatStr.equalsIgnoreCase("DATE")){
                    df=new DateTimeDateFormat();
                }
                else{
                    try{
                        df=new SimpleDateFormat(dateFormatStr);
                    }
                    catch(IllegalArgumentException e){
                        LogLog.error("Could not instantiate SimpleDateFormat with "+dateFormatStr,e);
                        df=(DateFormat)OptionConverter.instantiateByClassName("org.apache.log4j.helpers.ISO8601DateFormat",(PatternParser.class$java$text$DateFormat==null)?(PatternParser.class$java$text$DateFormat=class$("java.text.DateFormat")):PatternParser.class$java$text$DateFormat,null);
                    }
                }
                pc=new DatePatternConverter(this.formattingInfo,df);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'F':{
                pc=new LocationPatternConverter(this.formattingInfo,1004);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'l':{
                pc=new LocationPatternConverter(this.formattingInfo,1000);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'L':{
                pc=new LocationPatternConverter(this.formattingInfo,1003);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'm':{
                pc=new BasicPatternConverter(this.formattingInfo,2004);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'M':{
                pc=new LocationPatternConverter(this.formattingInfo,1001);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'p':{
                pc=new BasicPatternConverter(this.formattingInfo,2002);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'r':{
                pc=new BasicPatternConverter(this.formattingInfo,2000);
                this.currentLiteral.setLength(0);
                break;
            }
            case 't':{
                pc=new BasicPatternConverter(this.formattingInfo,2001);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'x':{
                pc=new BasicPatternConverter(this.formattingInfo,2003);
                this.currentLiteral.setLength(0);
                break;
            }
            case 'X':{
                final String xOpt=this.extractOption();
                pc=new MDCPatternConverter(this.formattingInfo,xOpt);
                this.currentLiteral.setLength(0);
                break;
            }
            default:{
                LogLog.error("Unexpected char ["+c+"] at position "+this.i+" in conversion patterrn.");
                pc=new LiteralPatternConverter(this.currentLiteral.toString());
                this.currentLiteral.setLength(0);
                break;
            }
        }
        this.addConverter(pc);
    }
    protected void addConverter(final PatternConverter pc){
        this.currentLiteral.setLength(0);
        this.addToList(pc);
        this.state=0;
        this.formattingInfo.reset();
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    private static class BasicPatternConverter extends PatternConverter{
        int type;
        BasicPatternConverter(final FormattingInfo formattingInfo,final int type){
            super(formattingInfo);
            this.type=type;
        }
        public String convert(final LoggingEvent event){
            switch(this.type){
                case 2000:{
                    return Long.toString(event.timeStamp-LoggingEvent.getStartTime());
                }
                case 2001:{
                    return event.getThreadName();
                }
                case 2002:{
                    return event.getLevel().toString();
                }
                case 2003:{
                    return event.getNDC();
                }
                case 2004:{
                    return event.getRenderedMessage();
                }
                default:{
                    return null;
                }
            }
        }
    }
    private static class LiteralPatternConverter extends PatternConverter{
        private String literal;
        LiteralPatternConverter(final String value){
            super();
            this.literal=value;
        }
        public final void format(final StringBuffer sbuf,final LoggingEvent event){
            sbuf.append(this.literal);
        }
        public String convert(final LoggingEvent event){
            return this.literal;
        }
    }
    private static class DatePatternConverter extends PatternConverter{
        private DateFormat df;
        private Date date;
        DatePatternConverter(final FormattingInfo formattingInfo,final DateFormat df){
            super(formattingInfo);
            this.date=new Date();
            this.df=df;
        }
        public String convert(final LoggingEvent event){
            this.date.setTime(event.timeStamp);
            String converted=null;
            try{
                converted=this.df.format(this.date);
            }
            catch(Exception ex){
                LogLog.error("Error occured while converting date.",ex);
            }
            return converted;
        }
    }
    private static class MDCPatternConverter extends PatternConverter{
        private String key;
        MDCPatternConverter(final FormattingInfo formattingInfo,final String key){
            super(formattingInfo);
            this.key=key;
        }
        public String convert(final LoggingEvent event){
            final Object val=event.getMDC(this.key);
            if(val==null){
                return null;
            }
            return val.toString();
        }
    }
    private class LocationPatternConverter extends PatternConverter{
        int type;
        LocationPatternConverter(final FormattingInfo formattingInfo,final int type){
            super(formattingInfo);
            this.type=type;
        }
        public String convert(final LoggingEvent event){
            final LocationInfo locationInfo=event.getLocationInformation();
            switch(this.type){
                case 1000:{
                    return locationInfo.fullInfo;
                }
                case 1001:{
                    return locationInfo.getMethodName();
                }
                case 1003:{
                    return locationInfo.getLineNumber();
                }
                case 1004:{
                    return locationInfo.getFileName();
                }
                default:{
                    return null;
                }
            }
        }
    }
    private abstract static class NamedPatternConverter extends PatternConverter{
        int precision;
        NamedPatternConverter(final FormattingInfo formattingInfo,final int precision){
            super(formattingInfo);
            this.precision=precision;
        }
        abstract String getFullyQualifiedName(final LoggingEvent p0);
        public String convert(final LoggingEvent event){
            final String n=this.getFullyQualifiedName(event);
            if(this.precision<=0){
                return n;
            }
            final int len=n.length();
            int end=len-1;
            for(int i=this.precision;i>0;--i){
                end=n.lastIndexOf(46,end-1);
                if(end==-1){
                    return n;
                }
            }
            return n.substring(end+1,len);
        }
    }
    private class ClassNamePatternConverter extends NamedPatternConverter{
        ClassNamePatternConverter(final FormattingInfo formattingInfo,final int precision){
            super(formattingInfo,precision);
        }
        String getFullyQualifiedName(final LoggingEvent event){
            return event.getLocationInformation().getClassName();
        }
    }
    private class CategoryPatternConverter extends NamedPatternConverter{
        CategoryPatternConverter(final FormattingInfo formattingInfo,final int precision){
            super(formattingInfo,precision);
        }
        String getFullyQualifiedName(final LoggingEvent event){
            return event.getLoggerName();
        }
    }
}
