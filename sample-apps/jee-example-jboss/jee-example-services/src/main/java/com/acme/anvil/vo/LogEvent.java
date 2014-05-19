package com.acme.anvil.vo;

import java.io.Serializable;
import java.util.Date;

public class LogEvent implements Serializable {

	public LogEvent(Date date, String message) {
		this.date = date;
		this.message = message;
	}
	
	private Date date;
	private String message;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}
