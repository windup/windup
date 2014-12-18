package org.apache.log4j.spi;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public final class RootCategory extends Logger{
    public RootCategory(final Level level){
        super("root");
        this.setLevel(level);
    }
    public final Level getChainedLevel(){
        return super.level;
    }
    public final void setLevel(final Level level){
        if(level==null){
            LogLog.error("You have tried to set a null level to root.",new Throwable());
        }
        else{
            super.level=level;
        }
    }
    public final void setPriority(final Level level){
        this.setLevel(level);
    }
}
