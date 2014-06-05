package org.apache.wicket.util.cookies;

import javax.servlet.http.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.protocol.http.servlet.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.util.time.*;
import org.slf4j.*;

public class CookieUtils{
    private static final Logger log;
    private final CookieDefaults settings;
    public CookieUtils(){
        super();
        this.settings=new CookieDefaults();
    }
    public CookieUtils(final CookieDefaults settings){
        super();
        this.settings=settings;
    }
    public final CookieDefaults getSettings(){
        return this.settings;
    }
    public final void remove(final String key){
        final Cookie cookie=this.getCookie(key);
        if(cookie!=null){
            this.remove(cookie);
        }
    }
    public final void remove(final FormComponent<?> formComponent){
        this.remove(this.getKey(formComponent));
    }
    protected String getKey(final FormComponent<?> component){
        return component.getPageRelativePath();
    }
    public final String load(final String key){
        final Cookie cookie=this.getCookie(key);
        if(cookie!=null){
            return cookie.getValue();
        }
        return null;
    }
    public final String load(final FormComponent<?> formComponent){
        final String value=this.load(this.getKey(formComponent));
        if(value!=null){
            formComponent.setModelValue(this.splitValue(value));
        }
        return value;
    }
    protected String[] splitValue(final String value){
        return value.split(";");
    }
    protected String joinValues(final String... values){
        return Strings.join(";",values);
    }
    public final void save(String key,final String... values){
        key=this.getSaveKey(key);
        final String value=this.joinValues(values);
        Cookie cookie=this.getCookie(key);
        if(cookie==null){
            cookie=new Cookie(key,value);
        }
        else{
            cookie.setValue(value);
        }
        cookie.setSecure(false);
        cookie.setMaxAge(this.settings.getMaxAge());
        this.save(cookie);
    }
    public final void save(final FormComponent<?> formComponent){
        this.save(this.getKey(formComponent),formComponent.getValue());
    }
    protected String getSaveKey(String key){
        if(Strings.isEmpty((CharSequence)key)){
            throw new IllegalArgumentException("A Cookie name can not be null or empty");
        }
        key=key.replace((CharSequence)".",(CharSequence)"..");
        key=key.replace((CharSequence)":",(CharSequence)".");
        return key;
    }
    private void remove(final Cookie cookie){
        if(cookie!=null){
            cookie.setMaxAge(0);
            cookie.setValue(null);
            this.save(cookie);
            if(CookieUtils.log.isDebugEnabled()){
                CookieUtils.log.debug("Removed Cookie: "+cookie.getName());
            }
        }
    }
    private Cookie getCookie(final String name){
        final String key=this.getSaveKey(name);
        try{
            final Cookie cookie=this.getWebRequest().getCookie(key);
            if(CookieUtils.log.isDebugEnabled()){
                if(cookie!=null){
                    CookieUtils.log.debug("Found Cookie with name="+key+" and request URI="+this.getWebRequest().getUrl().toString());
                }
                else{
                    CookieUtils.log.debug("Unable to find Cookie with name="+key+" and request URI="+this.getWebRequest().getUrl().toString());
                }
            }
            return cookie;
        }
        catch(NullPointerException ex){
            return null;
        }
    }
    private Cookie save(final Cookie cookie){
        if(cookie==null){
            return null;
        }
        this.initializeCookie(cookie);
        this.getWebResponse().addCookie(cookie);
        if(CookieUtils.log.isDebugEnabled()){
            CookieUtils.log.debug("Cookie saved: "+this.cookieToDebugString(cookie)+"; request URI="+this.getWebRequest().getUrl().toString());
        }
        return cookie;
    }
    protected void initializeCookie(final Cookie cookie){
        final String comment=this.settings.getComment();
        if(comment!=null){
            cookie.setComment(comment);
        }
        final String domain=this.settings.getDomain();
        if(domain!=null){
            cookie.setDomain(domain);
        }
        final ServletWebRequest request=(ServletWebRequest)this.getWebRequest();
        final String path=request.getContainerRequest().getContextPath()+"/"+request.getFilterPrefix();
        cookie.setPath(path);
        cookie.setVersion(this.settings.getVersion());
        cookie.setSecure(this.settings.getSecure());
    }
    private WebRequest getWebRequest(){
        return (WebRequest)RequestCycle.get().getRequest();
    }
    private WebResponse getWebResponse(){
        return (WebResponse)RequestCycle.get().getResponse();
    }
    private String cookieToDebugString(final Cookie cookie){
        return "[Cookie  name = "+cookie.getName()+", value = "+cookie.getValue()+", domain = "+cookie.getDomain()+", path = "+cookie.getPath()+", maxAge = "+Time.millis((long)cookie.getMaxAge()).toDateString()+"("+cookie.getMaxAge()+")"+"]";
    }
    static{
        log=LoggerFactory.getLogger(CookieUtils.class);
    }
}
