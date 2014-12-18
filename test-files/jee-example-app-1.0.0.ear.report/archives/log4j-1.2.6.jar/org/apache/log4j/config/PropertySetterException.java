package org.apache.log4j.config;

public class PropertySetterException extends Exception{
    protected Throwable rootCause;
    public PropertySetterException(final String msg){
        super(msg);
    }
    public PropertySetterException(final Throwable rootCause){
        super();
        this.rootCause=rootCause;
    }
    public String getMessage(){
        String msg=super.getMessage();
        if(msg==null&&this.rootCause!=null){
            msg=this.rootCause.getMessage();
        }
        return msg;
    }
}
