package org.apache.wicket.feedback;

import org.apache.wicket.model.*;
import java.io.*;
import org.apache.wicket.*;
import java.util.*;

public class FeedbackMessage implements IDetachable{
    private static final long serialVersionUID=1L;
    public static final int UNDEFINED=0;
    public static final int DEBUG=100;
    public static final int INFO=200;
    public static final int SUCCESS=250;
    public static final int WARNING=300;
    public static final int ERROR=400;
    public static final int FATAL=500;
    private static final Map<Integer,String> levelStrings;
    private final int level;
    private final Serializable message;
    private Component reporter;
    private boolean rendered;
    public FeedbackMessage(final Component reporter,final Serializable message,final int level){
        super();
        this.rendered=false;
        if(message==null){
            throw new IllegalArgumentException("Parameter message can't be null");
        }
        this.reporter=reporter;
        this.message=message;
        this.level=level;
    }
    public final boolean isRendered(){
        return this.rendered;
    }
    public final void markRendered(){
        this.rendered=true;
    }
    public final int getLevel(){
        return this.level;
    }
    public String getLevelAsString(){
        return (String)FeedbackMessage.levelStrings.get(this.getLevel());
    }
    public final Serializable getMessage(){
        return this.message;
    }
    public final Component getReporter(){
        return this.reporter;
    }
    public final boolean isDebug(){
        return this.isLevel(100);
    }
    public final boolean isInfo(){
        return this.isLevel(200);
    }
    public final boolean isSuccess(){
        return this.isLevel(250);
    }
    public final boolean isWarning(){
        return this.isLevel(300);
    }
    public final boolean isError(){
        return this.isLevel(400);
    }
    public final boolean isFatal(){
        return this.isLevel(500);
    }
    public final boolean isLevel(final int level){
        return this.getLevel()>=level;
    }
    public final boolean isUndefined(){
        return this.getLevel()==0;
    }
    public String toString(){
        return "[FeedbackMessage message = \""+this.getMessage()+"\", reporter = "+((this.getReporter()==null)?"null":this.getReporter().getId())+", level = "+this.getLevelAsString()+']';
    }
    public void detach(){
        this.reporter=null;
    }
    static{
        (levelStrings=new HashMap()).put(0,"UNDEFINED");
        FeedbackMessage.levelStrings.put(100,"DEBUG");
        FeedbackMessage.levelStrings.put(200,"INFO");
        FeedbackMessage.levelStrings.put(250,"SUCCESS");
        FeedbackMessage.levelStrings.put(300,"WARNING");
        FeedbackMessage.levelStrings.put(400,"ERROR");
        FeedbackMessage.levelStrings.put(500,"FATAL");
    }
}
