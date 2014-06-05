package org.apache.wicket.markup.html.link;

import org.apache.wicket.*;
import org.slf4j.*;

public class PopupSettings implements IClusterable{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public static final int LOCATION_BAR=1;
    public static final int MENU_BAR=2;
    public static final int RESIZABLE=4;
    public static final int SCROLLBARS=8;
    public static final int STATUS_BAR=16;
    public static final int TOOL_BAR=32;
    private int displayFlags;
    private int height;
    private int left;
    private String target;
    private int top;
    private int width;
    private String windowName;
    public PopupSettings(){
        super();
        this.height=-1;
        this.left=-1;
        this.target="href";
        this.top=-1;
        this.width=-1;
        this.windowName=null;
    }
    public PopupSettings(final int displayFlags){
        this(null,displayFlags);
    }
    public PopupSettings(final String windowName){
        this(windowName,0);
    }
    public PopupSettings(final String windowName,final int displayFlags){
        super();
        this.height=-1;
        this.left=-1;
        this.target="href";
        this.top=-1;
        this.width=-1;
        this.windowName=null;
        this.displayFlags=displayFlags;
        this.windowName=windowName;
    }
    public String getPopupJavaScript(){
        String windowTitle=this.windowName;
        if(windowTitle==null){
            windowTitle="";
        }
        else{
            windowTitle=windowTitle.replaceAll("\\W","_");
        }
        final StringBuilder script=new StringBuilder("var w = window.open("+this.target+", '").append(windowTitle).append("', '");
        script.append("scrollbars=").append(this.flagToString(8));
        script.append(",location=").append(this.flagToString(1));
        script.append(",menuBar=").append(this.flagToString(2));
        script.append(",resizable=").append(this.flagToString(4));
        script.append(",status=").append(this.flagToString(16));
        script.append(",toolbar=").append(this.flagToString(32));
        if(this.width!=-1){
            script.append(",width=").append(this.width);
        }
        if(this.height!=-1){
            script.append(",height=").append(this.height);
        }
        if(this.left!=-1){
            script.append(",left=").append(this.left);
        }
        if(this.top!=-1){
            script.append(",top=").append(this.top);
        }
        script.append("'); if(w.blur) w.focus();").append(" return false;");
        return script.toString();
    }
    public PopupSettings setHeight(final int popupHeight){
        this.height=popupHeight;
        return this;
    }
    public PopupSettings setLeft(final int popupPositionLeft){
        this.left=popupPositionLeft;
        return this;
    }
    public void setTarget(final String target){
        this.target=target;
    }
    public PopupSettings setTop(final int popupPositionTop){
        this.top=popupPositionTop;
        return this;
    }
    public PopupSettings setWidth(final int popupWidth){
        this.width=popupWidth;
        return this;
    }
    public PopupSettings setWindowName(final String popupWindowName){
        if(popupWindowName!=null){
            this.windowName=popupWindowName;
        }
        return this;
    }
    private String flagToString(final int flag){
        return ((this.displayFlags&flag)!=0x0)?"yes":"no";
    }
    static{
        log=LoggerFactory.getLogger(PopupSettings.class);
    }
}
