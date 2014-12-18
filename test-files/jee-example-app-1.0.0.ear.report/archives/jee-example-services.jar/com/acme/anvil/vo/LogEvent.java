package com.acme.anvil.vo;

import java.util.Date;
import java.io.Serializable;

public class LogEvent implements Serializable{
    private Date date;
    private String message;
    public LogEvent(final Date date,final String message){
        super();
        this.date=date;
        this.message=message;
    }
    public Date getDate(){
        return this.date;
    }
    public void setDate(final Date date){
        this.date=date;
    }
    public String getMessage(){
        return this.message;
    }
    public void setMessage(final String message){
        this.message=message;
    }
}
