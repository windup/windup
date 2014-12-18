package org.apache.log4j;

import java.util.Date;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Layout;

public class HTMLLayout extends Layout{
    protected final int BUF_SIZE=256;
    protected final int MAX_CAPACITY=1024;
    static String TRACE_PREFIX;
    private StringBuffer sbuf;
    public static final String LOCATION_INFO_OPTION="LocationInfo";
    public static final String TITLE_OPTION="Title";
    boolean locationInfo;
    String title;
    public HTMLLayout(){
        super();
        this.sbuf=new StringBuffer(256);
        this.locationInfo=false;
        this.title="Log4J Log Messages";
    }
    public void setLocationInfo(final boolean flag){
        this.locationInfo=flag;
    }
    public boolean getLocationInfo(){
        return this.locationInfo;
    }
    public void setTitle(final String title){
        this.title=title;
    }
    public String getTitle(){
        return this.title;
    }
    public String getContentType(){
        return "text/html";
    }
    public void activateOptions(){
    }
    public String format(final LoggingEvent event){
        if(this.sbuf.capacity()>1024){
            this.sbuf=new StringBuffer(256);
        }
        else{
            this.sbuf.setLength(0);
        }
        this.sbuf.append(Layout.LINE_SEP+"<tr>"+Layout.LINE_SEP);
        this.sbuf.append("<td>");
        this.sbuf.append(event.timeStamp-LoggingEvent.getStartTime());
        this.sbuf.append("</td>"+Layout.LINE_SEP);
        this.sbuf.append("<td title=\""+event.getThreadName()+" thread\">");
        this.sbuf.append(Transform.escapeTags(event.getThreadName()));
        this.sbuf.append("</td>"+Layout.LINE_SEP);
        this.sbuf.append("<td title=\"Level\">");
        if(event.getLevel().equals(Level.DEBUG)){
            this.sbuf.append("<font color=\"#339933\">");
            this.sbuf.append(event.getLevel());
            this.sbuf.append("</font>");
        }
        else if(event.getLevel().isGreaterOrEqual(Level.WARN)){
            this.sbuf.append("<font color=\"#993300\"><strong>");
            this.sbuf.append(event.getLevel());
            this.sbuf.append("</strong></font>");
        }
        else{
            this.sbuf.append(event.getLevel());
        }
        this.sbuf.append("</td>"+Layout.LINE_SEP);
        this.sbuf.append("<td title=\""+event.getLoggerName()+" category\">");
        this.sbuf.append(Transform.escapeTags(event.getLoggerName()));
        this.sbuf.append("</td>"+Layout.LINE_SEP);
        if(this.locationInfo){
            final LocationInfo locInfo=event.getLocationInformation();
            this.sbuf.append("<td>");
            this.sbuf.append(Transform.escapeTags(locInfo.getFileName()));
            this.sbuf.append(':');
            this.sbuf.append(locInfo.getLineNumber());
            this.sbuf.append("</td>"+Layout.LINE_SEP);
        }
        this.sbuf.append("<td title=\"Message\">");
        this.sbuf.append(Transform.escapeTags(event.getRenderedMessage()));
        this.sbuf.append("</td>"+Layout.LINE_SEP);
        this.sbuf.append("</tr>"+Layout.LINE_SEP);
        if(event.getNDC()!=null){
            this.sbuf.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">");
            this.sbuf.append("NDC: "+Transform.escapeTags(event.getNDC()));
            this.sbuf.append("</td></tr>"+Layout.LINE_SEP);
        }
        final String[] s=event.getThrowableStrRep();
        if(s!=null){
            this.sbuf.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">");
            this.appendThrowableAsHTML(s,this.sbuf);
            this.sbuf.append("</td></tr>"+Layout.LINE_SEP);
        }
        return this.sbuf.toString();
    }
    void appendThrowableAsHTML(final String[] s,final StringBuffer sbuf){
        if(s!=null){
            final int len=s.length;
            if(len==0){
                return;
            }
            sbuf.append(Transform.escapeTags(s[0]));
            sbuf.append(Layout.LINE_SEP);
            for(int i=1;i<len;++i){
                sbuf.append(HTMLLayout.TRACE_PREFIX);
                sbuf.append(Transform.escapeTags(s[i]));
                sbuf.append(Layout.LINE_SEP);
            }
        }
    }
    public String getHeader(){
        final StringBuffer sbuf=new StringBuffer();
        sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"+Layout.LINE_SEP);
        sbuf.append("<html>"+Layout.LINE_SEP);
        sbuf.append("<head>"+Layout.LINE_SEP);
        sbuf.append("<title>"+this.title+"</title>"+Layout.LINE_SEP);
        sbuf.append("<style type=\"text/css\">"+Layout.LINE_SEP);
        sbuf.append("<!--"+Layout.LINE_SEP);
        sbuf.append("body, table {font-family: arial,sans-serif; font-size: x-small;}"+Layout.LINE_SEP);
        sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}"+Layout.LINE_SEP);
        sbuf.append("-->"+Layout.LINE_SEP);
        sbuf.append("</style>"+Layout.LINE_SEP);
        sbuf.append("</head>"+Layout.LINE_SEP);
        sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">"+Layout.LINE_SEP);
        sbuf.append("<hr size=\"1\" noshade>"+Layout.LINE_SEP);
        sbuf.append("Log session start time "+new Date()+"<br>"+Layout.LINE_SEP);
        sbuf.append("<br>"+Layout.LINE_SEP);
        sbuf.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">"+Layout.LINE_SEP);
        sbuf.append("<tr>"+Layout.LINE_SEP);
        sbuf.append("<th>Time</th>"+Layout.LINE_SEP);
        sbuf.append("<th>Thread</th>"+Layout.LINE_SEP);
        sbuf.append("<th>Level</th>"+Layout.LINE_SEP);
        sbuf.append("<th>Category</th>"+Layout.LINE_SEP);
        if(this.locationInfo){
            sbuf.append("<th>File:Line</th>"+Layout.LINE_SEP);
        }
        sbuf.append("<th>Message</th>"+Layout.LINE_SEP);
        sbuf.append("</tr>"+Layout.LINE_SEP);
        return sbuf.toString();
    }
    public String getFooter(){
        final StringBuffer sbuf=new StringBuffer();
        sbuf.append("</table>"+Layout.LINE_SEP);
        sbuf.append("<br>"+Layout.LINE_SEP);
        sbuf.append("</body></html>");
        return sbuf.toString();
    }
    public boolean ignoresThrowable(){
        return false;
    }
    static{
        HTMLLayout.TRACE_PREFIX="<br>&nbsp;&nbsp;&nbsp;&nbsp;";
    }
}
