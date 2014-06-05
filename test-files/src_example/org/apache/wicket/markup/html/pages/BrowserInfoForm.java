package org.apache.wicket.markup.html.pages;

import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.model.*;
import org.apache.wicket.protocol.http.request.*;
import org.apache.wicket.request.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.markup.html.form.*;
import org.slf4j.*;
import org.apache.wicket.*;

public class BrowserInfoForm extends Panel{
    private static final Logger log;
    private static final long serialVersionUID=1L;
    public BrowserInfoForm(final String id){
        super(id);
        final Form<ClientPropertiesBean> form=new Form<ClientPropertiesBean>("postback",new CompoundPropertyModel(new ClientPropertiesBean())){
            private static final long serialVersionUID=1L;
            protected void onSubmit(){
                final ClientPropertiesBean propertiesBean=this.getModelObject();
                final RequestCycle requestCycle=this.getRequestCycle();
                final WebSession session=(WebSession)this.getSession();
                WebClientInfo clientInfo=session.getClientInfo();
                if(clientInfo==null){
                    clientInfo=new WebClientInfo(requestCycle);
                    this.getSession().setClientInfo(clientInfo);
                }
                final ClientProperties properties=clientInfo.getProperties();
                propertiesBean.merge(properties);
                BrowserInfoForm.this.afterSubmit();
            }
        };
        form.add(new TextField<Object>("navigatorAppName"));
        form.add(new TextField<Object>("navigatorAppVersion"));
        form.add(new TextField<Object>("navigatorAppCodeName"));
        form.add(new TextField<Object>("navigatorCookieEnabled"));
        form.add(new TextField<Object>("navigatorJavaEnabled"));
        form.add(new TextField<Object>("navigatorLanguage"));
        form.add(new TextField<Object>("navigatorPlatform"));
        form.add(new TextField<Object>("navigatorUserAgent"));
        form.add(new TextField<Object>("screenWidth"));
        form.add(new TextField<Object>("screenHeight"));
        form.add(new TextField<Object>("screenColorDepth"));
        form.add(new TextField<Object>("utcOffset"));
        form.add(new TextField<Object>("utcDSTOffset"));
        form.add(new TextField<Object>("browserWidth"));
        form.add(new TextField<Object>("browserHeight"));
        form.add(new TextField<Object>("hostname"));
        this.add(form);
    }
    void warnNotUsingWebClientInfo(final ClientInfo clientInfo){
        BrowserInfoForm.log.warn("using "+this.getClass().getName()+" makes no sense if you are not using "+WebClientInfo.class.getName()+" (you are using "+clientInfo.getClass().getName()+" instead)");
    }
    protected void afterSubmit(){
    }
    static{
        log=LoggerFactory.getLogger(BrowserInfoForm.class);
    }
    public static class ClientPropertiesBean implements IClusterable{
        private static final long serialVersionUID=1L;
        private String navigatorAppCodeName;
        private String navigatorAppName;
        private String navigatorAppVersion;
        private Boolean navigatorCookieEnabled;
        private Boolean navigatorJavaEnabled;
        private String navigatorLanguage;
        private String navigatorPlatform;
        private String navigatorUserAgent;
        private String screenColorDepth;
        private String screenHeight;
        private String screenWidth;
        private String utcOffset;
        private String utcDSTOffset;
        private String browserWidth;
        private String browserHeight;
        private String hostname;
        public ClientPropertiesBean(){
            super();
            this.navigatorCookieEnabled=Boolean.FALSE;
            this.navigatorJavaEnabled=Boolean.FALSE;
        }
        public String getBrowserHeight(){
            return this.browserHeight;
        }
        public String getBrowserWidth(){
            return this.browserWidth;
        }
        public String getNavigatorAppCodeName(){
            return this.navigatorAppCodeName;
        }
        public String getNavigatorAppName(){
            return this.navigatorAppName;
        }
        public String getNavigatorAppVersion(){
            return this.navigatorAppVersion;
        }
        public Boolean getNavigatorCookieEnabled(){
            return this.navigatorCookieEnabled;
        }
        public Boolean getNavigatorJavaEnabled(){
            return this.navigatorJavaEnabled;
        }
        public String getNavigatorLanguage(){
            return this.navigatorLanguage;
        }
        public String getNavigatorPlatform(){
            return this.navigatorPlatform;
        }
        public String getNavigatorUserAgent(){
            return this.navigatorUserAgent;
        }
        public String getScreenColorDepth(){
            return this.screenColorDepth;
        }
        public String getScreenHeight(){
            return this.screenHeight;
        }
        public String getScreenWidth(){
            return this.screenWidth;
        }
        public String getUtcOffset(){
            return this.utcOffset;
        }
        public String getUtcDSTOffset(){
            return this.utcDSTOffset;
        }
        public void merge(final ClientProperties properties){
            properties.setNavigatorAppName(this.navigatorAppName);
            properties.setNavigatorAppVersion(this.navigatorAppVersion);
            properties.setNavigatorAppCodeName(this.navigatorAppCodeName);
            properties.setCookiesEnabled(this.navigatorCookieEnabled!=null&&this.navigatorCookieEnabled);
            properties.setJavaEnabled(this.navigatorJavaEnabled!=null&&this.navigatorJavaEnabled);
            properties.setNavigatorLanguage(this.navigatorLanguage);
            properties.setNavigatorPlatform(this.navigatorPlatform);
            properties.setNavigatorUserAgent(this.navigatorUserAgent);
            properties.setScreenWidth(this.getInt(this.screenWidth));
            properties.setScreenHeight(this.getInt(this.screenHeight));
            properties.setBrowserWidth(this.getInt(this.browserWidth));
            properties.setBrowserHeight(this.getInt(this.browserHeight));
            properties.setScreenColorDepth(this.getInt(this.screenColorDepth));
            properties.setUtcOffset(this.utcOffset);
            properties.setUtcDSTOffset(this.utcDSTOffset);
            properties.setHostname(this.hostname);
        }
        public void setBrowserHeight(final String browserHeight){
            this.browserHeight=browserHeight;
        }
        public void setBrowserWidth(final String browserWidth){
            this.browserWidth=browserWidth;
        }
        public void setNavigatorAppCodeName(final String navigatorAppCodeName){
            this.navigatorAppCodeName=navigatorAppCodeName;
        }
        public void setNavigatorAppName(final String navigatorAppName){
            this.navigatorAppName=navigatorAppName;
        }
        public void setNavigatorAppVersion(final String navigatorAppVersion){
            this.navigatorAppVersion=navigatorAppVersion;
        }
        public void setNavigatorCookieEnabled(final Boolean navigatorCookieEnabled){
            this.navigatorCookieEnabled=navigatorCookieEnabled;
        }
        public void setNavigatorJavaEnabled(final Boolean navigatorJavaEnabled){
            this.navigatorJavaEnabled=navigatorJavaEnabled;
        }
        public void setNavigatorLanguage(final String navigatorLanguage){
            this.navigatorLanguage=navigatorLanguage;
        }
        public void setNavigatorPlatform(final String navigatorPlatform){
            this.navigatorPlatform=navigatorPlatform;
        }
        public void setNavigatorUserAgent(final String navigatorUserAgent){
            this.navigatorUserAgent=navigatorUserAgent;
        }
        public void setScreenColorDepth(final String screenColorDepth){
            this.screenColorDepth=screenColorDepth;
        }
        public void setScreenHeight(final String screenHeight){
            this.screenHeight=screenHeight;
        }
        public void setScreenWidth(final String screenWidth){
            this.screenWidth=screenWidth;
        }
        public void setHostname(final String hostname){
            this.hostname=hostname;
        }
        public String getHostname(){
            return this.hostname;
        }
        public void setUtcOffset(final String utcOffset){
            this.utcOffset=utcOffset;
        }
        public void setUtcDSTOffset(final String utcDSTOffset){
            this.utcDSTOffset=utcDSTOffset;
        }
        private int getInt(final String value){
            int intValue=-1;
            try{
                intValue=Integer.parseInt(value);
            }
            catch(NumberFormatException ex){
            }
            return intValue;
        }
    }
}
