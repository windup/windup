package org.apache.wicket.util.cookies;

import org.apache.wicket.*;

public class CookieDefaults implements IClusterable{
    private static final long serialVersionUID=1L;
    private int maxAge;
    private String comment;
    private String domain;
    private boolean secure;
    private int version;
    public CookieDefaults(){
        super();
        this.maxAge=2592000;
    }
    public int getMaxAge(){
        return this.maxAge;
    }
    public void setMaxAge(final int maxAge){
        this.maxAge=maxAge;
    }
    public String getComment(){
        return this.comment;
    }
    public void setComment(final String comment){
        this.comment=comment;
    }
    public String getDomain(){
        return this.domain;
    }
    public void setDomain(final String domain){
        this.domain=domain;
    }
    public boolean getSecure(){
        return this.secure;
    }
    public void setSecure(final boolean secure){
        this.secure=secure;
    }
    public int getVersion(){
        return this.version;
    }
    public void setVersion(final int version){
        this.version=version;
    }
}
