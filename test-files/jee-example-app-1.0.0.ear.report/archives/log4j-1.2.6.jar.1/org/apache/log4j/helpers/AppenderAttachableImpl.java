package org.apache.log4j.helpers;

import java.util.Enumeration;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Appender;
import java.util.Vector;
import org.apache.log4j.spi.AppenderAttachable;

public class AppenderAttachableImpl implements AppenderAttachable{
    protected Vector appenderList;
    public void addAppender(final Appender newAppender){
        if(newAppender==null){
            return;
        }
        if(this.appenderList==null){
            this.appenderList=new Vector(1);
        }
        if(!this.appenderList.contains(newAppender)){
            this.appenderList.addElement(newAppender);
        }
    }
    public int appendLoopOnAppenders(final LoggingEvent event){
        int size=0;
        if(this.appenderList!=null){
            size=this.appenderList.size();
            for(int i=0;i<size;++i){
                final Appender appender=this.appenderList.elementAt(i);
                appender.doAppend(event);
            }
        }
        return size;
    }
    public Enumeration getAllAppenders(){
        if(this.appenderList==null){
            return null;
        }
        return this.appenderList.elements();
    }
    public Appender getAppender(final String name){
        if(this.appenderList==null||name==null){
            return null;
        }
        for(int size=this.appenderList.size(),i=0;i<size;++i){
            final Appender appender=this.appenderList.elementAt(i);
            if(name.equals(appender.getName())){
                return appender;
            }
        }
        return null;
    }
    public boolean isAttached(final Appender appender){
        if(this.appenderList==null||appender==null){
            return false;
        }
        for(int size=this.appenderList.size(),i=0;i<size;++i){
            final Appender a=this.appenderList.elementAt(i);
            if(a==appender){
                return true;
            }
        }
        return false;
    }
    public void removeAllAppenders(){
        if(this.appenderList!=null){
            for(int len=this.appenderList.size(),i=0;i<len;++i){
                final Appender a=this.appenderList.elementAt(i);
                a.close();
            }
            this.appenderList.removeAllElements();
            this.appenderList=null;
        }
    }
    public void removeAppender(final Appender appender){
        if(appender==null||this.appenderList==null){
            return;
        }
        this.appenderList.removeElement(appender);
    }
    public void removeAppender(final String name){
        if(name==null||this.appenderList==null){
            return;
        }
        for(int size=this.appenderList.size(),i=0;i<size;++i){
            if(name.equals(this.appenderList.elementAt(i).getName())){
                this.appenderList.removeElementAt(i);
                break;
            }
        }
    }
}
