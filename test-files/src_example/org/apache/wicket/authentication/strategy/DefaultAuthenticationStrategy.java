package org.apache.wicket.authentication.strategy;

import org.apache.wicket.authentication.*;
import org.apache.wicket.util.cookies.*;
import org.apache.wicket.util.crypt.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;
import org.slf4j.*;

public class DefaultAuthenticationStrategy implements IAuthenticationStrategy{
    private static final Logger logger;
    private final String cookieKey;
    private final String VALUE_SEPARATOR="-sep-";
    private CookieUtils cookieUtils;
    private ICrypt crypt;
    public DefaultAuthenticationStrategy(final String cookieKey){
        super();
        if(Strings.isEmpty((CharSequence)cookieKey)){
            throw new IllegalArgumentException("Parameter 'cookieKey' must not be null or empty.");
        }
        this.cookieKey=cookieKey;
    }
    protected CookieUtils getCookieUtils(){
        if(this.cookieUtils==null){
            this.cookieUtils=new CookieUtils();
        }
        return this.cookieUtils;
    }
    protected ICrypt getCrypt(){
        if(this.crypt==null){
            this.crypt=Application.get().getSecuritySettings().getCryptFactory().newCrypt();
        }
        return this.crypt;
    }
    public String[] load(){
        String value=this.getCookieUtils().load(this.cookieKey);
        if(!Strings.isEmpty((CharSequence)value)){
            try{
                value=this.getCrypt().decryptUrlSafe(value);
            }
            catch(RuntimeException e){
                DefaultAuthenticationStrategy.logger.info("Error decrypting login cookie: {}. The cookie will be deleted. Possible cause is that a session-relative encryption key was used to encrypt this cookie while this decryption attempt is happening in a different session, eg user coming back to the application after session expiration",this.cookieKey);
                this.getCookieUtils().remove(this.cookieKey);
                value=null;
            }
            if(!Strings.isEmpty((CharSequence)value)){
                String username=null;
                String password=null;
                final String[] values=value.split("-sep-");
                if(values.length>0&&!Strings.isEmpty((CharSequence)values[0])){
                    username=values[0];
                }
                if(values.length>1&&!Strings.isEmpty((CharSequence)values[1])){
                    password=values[1];
                }
                return new String[] { username,password };
            }
        }
        return null;
    }
    public void save(final String username,final String password){
        final String value=username+"-sep-"+password;
        final String encryptedValue=this.getCrypt().encryptUrlSafe(value);
        this.getCookieUtils().save(this.cookieKey,encryptedValue);
    }
    public void remove(){
        this.getCookieUtils().remove(this.cookieKey);
    }
    static{
        logger=LoggerFactory.getLogger(DefaultAuthenticationStrategy.class);
    }
}
