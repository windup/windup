package org.apache.log4j.net;

import java.util.Hashtable;
import org.apache.log4j.helpers.OptionConverter;
import javax.mail.Transport;
import java.util.Date;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.log4j.spi.LoggingEvent;
import javax.mail.MessagingException;
import org.apache.log4j.helpers.LogLog;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.Session;
import java.util.Properties;
import org.apache.log4j.net.DefaultEvaluator;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import javax.mail.Message;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.AppenderSkeleton;

public class SMTPAppender extends AppenderSkeleton{
    private String to;
    private String from;
    private String subject;
    private String smtpHost;
    private int bufferSize;
    private boolean locationInfo;
    protected CyclicBuffer cb;
    protected Message msg;
    protected TriggeringEventEvaluator evaluator;
    static /* synthetic */ Class class$org$apache$log4j$spi$TriggeringEventEvaluator;
    public SMTPAppender(){
        this(new DefaultEvaluator());
    }
    public SMTPAppender(final TriggeringEventEvaluator evaluator){
        super();
        this.bufferSize=512;
        this.locationInfo=false;
        this.cb=new CyclicBuffer(this.bufferSize);
        this.evaluator=evaluator;
    }
    public void activateOptions(){
        final Properties properties=new Properties(System.getProperties());
        if(this.smtpHost!=null){
            ((Hashtable<String,String>)properties).put("mail.smtp.host",this.smtpHost);
        }
        this.msg=(Message)new MimeMessage(Session.getInstance(properties,(Authenticator)null));
        try{
            if(this.from!=null){
                this.msg.setFrom((Address)this.getAddress(this.from));
            }
            else{
                this.msg.setFrom();
            }
            this.msg.setRecipients(Message.RecipientType.TO,(Address[])this.parseAddress(this.to));
            if(this.subject!=null){
                this.msg.setSubject(this.subject);
            }
        }
        catch(MessagingException t){
            LogLog.error("Could not activate SMTPAppender options.",(Throwable)t);
        }
    }
    public void append(final LoggingEvent event){
        if(!this.checkEntryConditions()){
            return;
        }
        event.getThreadName();
        event.getNDC();
        if(this.locationInfo){
            event.getLocationInformation();
        }
        this.cb.add(event);
        if(this.evaluator.isTriggeringEvent(event)){
            this.sendBuffer();
        }
    }
    protected boolean checkEntryConditions(){
        if(this.msg==null){
            super.errorHandler.error("Message object not configured.");
            return false;
        }
        if(this.evaluator==null){
            super.errorHandler.error("No TriggeringEventEvaluator is set for appender ["+super.name+"].");
            return false;
        }
        if(super.layout==null){
            super.errorHandler.error("No layout set for appender named ["+super.name+"].");
            return false;
        }
        return true;
    }
    public synchronized void close(){
        super.closed=true;
    }
    InternetAddress getAddress(final String s){
        try{
            return new InternetAddress(s);
        }
        catch(AddressException ex){
            super.errorHandler.error("Could not parse address ["+s+"].",(Exception)ex,6);
            return null;
        }
    }
    InternetAddress[] parseAddress(final String s){
        try{
            return InternetAddress.parse(s,true);
        }
        catch(AddressException ex){
            super.errorHandler.error("Could not parse address ["+s+"].",(Exception)ex,6);
            return null;
        }
    }
    public String getTo(){
        return this.to;
    }
    public boolean requiresLayout(){
        return true;
    }
    protected void sendBuffer(){
        try{
            final MimeBodyPart mimeBodyPart=new MimeBodyPart();
            final StringBuffer sb=new StringBuffer();
            final String header=super.layout.getHeader();
            if(header!=null){
                sb.append(header);
            }
            for(int length=this.cb.length(),i=0;i<length;++i){
                final LoggingEvent value=this.cb.get();
                sb.append(super.layout.format(value));
                if(super.layout.ignoresThrowable()){
                    final String[] throwableStrRep=value.getThrowableStrRep();
                    if(throwableStrRep!=null){
                        for(int j=0;j<throwableStrRep.length;++j){
                            sb.append(throwableStrRep[j]);
                        }
                    }
                }
            }
            final String footer=super.layout.getFooter();
            if(footer!=null){
                sb.append(footer);
            }
            mimeBodyPart.setContent((Object)sb.toString(),super.layout.getContentType());
            final MimeMultipart content=new MimeMultipart();
            ((Multipart)content).addBodyPart((BodyPart)mimeBodyPart);
            this.msg.setContent((Multipart)content);
            this.msg.setSentDate(new Date());
            Transport.send(this.msg);
        }
        catch(Exception t){
            LogLog.error("Error occured while sending e-mail notification.",t);
        }
    }
    public String getEvaluatorClass(){
        return (this.evaluator==null)?null:this.evaluator.getClass().getName();
    }
    public String getFrom(){
        return this.from;
    }
    public String getSubject(){
        return this.subject;
    }
    public void setFrom(final String from){
        this.from=from;
    }
    public void setSubject(final String subject){
        this.subject=subject;
    }
    public void setBufferSize(final int n){
        this.bufferSize=n;
        this.cb.resize(n);
    }
    public void setSMTPHost(final String smtpHost){
        this.smtpHost=smtpHost;
    }
    public String getSMTPHost(){
        return this.smtpHost;
    }
    public void setTo(final String to){
        this.to=to;
    }
    public int getBufferSize(){
        return this.bufferSize;
    }
    public void setEvaluatorClass(final String className){
        this.evaluator=(TriggeringEventEvaluator)OptionConverter.instantiateByClassName(className,(SMTPAppender.class$org$apache$log4j$spi$TriggeringEventEvaluator==null)?(SMTPAppender.class$org$apache$log4j$spi$TriggeringEventEvaluator=class$("org.apache.log4j.spi.TriggeringEventEvaluator")):SMTPAppender.class$org$apache$log4j$spi$TriggeringEventEvaluator,this.evaluator);
    }
    public void setLocationInfo(final boolean locationInfo){
        this.locationInfo=locationInfo;
    }
    public boolean getLocationInfo(){
        return this.locationInfo;
    }
    static /* synthetic */ Class class$(final String s){
        try{
            return Class.forName(s);
        }
        catch(ClassNotFoundException ex){
            throw new NoClassDefFoundError(ex.getMessage());
        }
    }
}
