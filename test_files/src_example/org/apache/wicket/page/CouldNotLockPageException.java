package org.apache.wicket.page;

import org.apache.wicket.util.time.*;

public class CouldNotLockPageException extends RuntimeException{
    private static final long serialVersionUID=1L;
    private final int page;
    private final Duration timeout;
    private final String threadName;
    public CouldNotLockPageException(final int page,final String threadName,final Duration timeout){
        super("Could not lock page "+page+". Attempt lasted "+timeout);
        this.page=page;
        this.timeout=timeout;
        this.threadName=threadName;
    }
    public int getPage(){
        return this.page;
    }
    public Duration getTimeout(){
        return this.timeout;
    }
    public String getThreadName(){
        return this.threadName;
    }
}
