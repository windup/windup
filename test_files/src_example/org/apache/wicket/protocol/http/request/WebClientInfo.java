package org.apache.wicket.protocol.http.request;

import org.apache.wicket.request.*;
import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.protocol.http.servlet.*;
import java.net.*;
import javax.servlet.http.*;
import java.util.regex.*;
import org.slf4j.*;

public class WebClientInfo extends ClientInfo{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    private final String userAgent;
    private final ClientProperties properties;
    public WebClientInfo(final RequestCycle requestCycle){
        this(requestCycle,((ServletWebRequest)requestCycle.getRequest()).getContainerRequest().getHeader("User-Agent"));
    }
    public WebClientInfo(final RequestCycle requestCycle,final String userAgent){
        super();
        this.properties=new ClientProperties();
        this.userAgent=userAgent;
        this.properties.setRemoteAddress(this.getRemoteAddr(requestCycle));
        this.init();
    }
    public final ClientProperties getProperties(){
        return this.properties;
    }
    public final String getUserAgent(){
        return this.userAgent;
    }
    private String getUserAgentStringLc(){
        return (this.getUserAgent()!=null)?this.getUserAgent().toLowerCase():"";
    }
    protected String getRemoteAddr(final RequestCycle requestCycle){
        final ServletWebRequest request=(ServletWebRequest)requestCycle.getRequest();
        final HttpServletRequest req=request.getContainerRequest();
        String remoteAddr=request.getHeader("X-Forwarded-For");
        if(remoteAddr!=null){
            if(remoteAddr.contains((CharSequence)",")){
                remoteAddr=remoteAddr.split(",")[0].trim();
            }
            try{
                InetAddress.getByName(remoteAddr);
            }
            catch(UnknownHostException e){
                remoteAddr=req.getRemoteAddr();
            }
        }
        else{
            remoteAddr=req.getRemoteAddr();
        }
        return remoteAddr;
    }
    private void init(){
        this.setInternetExplorerProperties();
        this.setOperaProperties();
        this.setMozillaProperties();
        this.setKonquerorProperties();
        this.setChromeProperties();
        this.setSafariProperties();
        if(WebClientInfo.log.isDebugEnabled()){
            WebClientInfo.log.debug("determined user agent: "+this.properties);
        }
    }
    private void setKonquerorProperties(){
        this.properties.setBrowserKonqueror(UserAgent.KONQUEROR.matches(this.getUserAgent()));
        if(this.properties.isBrowserKonqueror()){
            this.setMajorMinorVersionByPattern("konqueror/(\\d+)\\.(\\d+)");
        }
    }
    private void setChromeProperties(){
        this.properties.setBrowserChrome(UserAgent.CHROME.matches(this.getUserAgent()));
        if(this.properties.isBrowserChrome()){
            this.setMajorMinorVersionByPattern("chrome/(\\d+)\\.(\\d+)");
        }
    }
    private void setSafariProperties(){
        this.properties.setBrowserSafari(UserAgent.SAFARI.matches(this.getUserAgent()));
        if(this.properties.isBrowserSafari()){
            final String userAgent=this.getUserAgentStringLc();
            if(userAgent.contains((CharSequence)"version/")){
                this.setMajorMinorVersionByPattern("version/(\\d+)\\.(\\d+)");
            }
        }
    }
    private void setMozillaProperties(){
        this.properties.setBrowserMozillaFirefox(UserAgent.FIREFOX.matches(this.getUserAgent()));
        this.properties.setBrowserMozilla(UserAgent.MOZILLA.matches(this.getUserAgent()));
        if(this.properties.isBrowserMozilla()){
            this.properties.setQuirkMozillaTextInputRepaint(true);
            this.properties.setQuirkMozillaPerformanceLargeDomRemove(true);
            if(this.properties.isBrowserMozillaFirefox()){
                this.setMajorMinorVersionByPattern("firefox/(\\d+)\\.(\\d+)");
            }
        }
    }
    private void setOperaProperties(){
        this.properties.setBrowserOpera(UserAgent.OPERA.matches(this.getUserAgent()));
        if(this.properties.isBrowserOpera()){
            final String userAgent=this.getUserAgentStringLc();
            if(userAgent.startsWith("opera/")&&userAgent.contains((CharSequence)"version/")){
                this.setMajorMinorVersionByPattern("version/(\\d+)\\.(\\d+)");
            }
            else if(userAgent.startsWith("opera/")&&!userAgent.contains((CharSequence)"version/")){
                this.setMajorMinorVersionByPattern("opera/(\\d+)\\.(\\d+)");
            }
            else{
                this.setMajorMinorVersionByPattern("opera (\\d+)\\.(\\d+)");
            }
        }
    }
    private void setInternetExplorerProperties(){
        this.properties.setBrowserInternetExplorer(UserAgent.INTERNET_EXPLORER.matches(this.getUserAgent()));
        if(this.properties.isBrowserInternetExplorer()){
            this.setMajorMinorVersionByPattern("msie (\\d+)\\.(\\d+)");
            this.properties.setProprietaryIECssExpressionsSupported(true);
            this.properties.setQuirkCssPositioningOneSideOnly(true);
            this.properties.setQuirkIERepaint(true);
            this.properties.setQuirkIESelectZIndex(true);
            this.properties.setQuirkIETextareaNewlineObliteration(true);
            this.properties.setQuirkIESelectPercentWidth(true);
            this.properties.setQuirkIESelectListDomUpdate(true);
            this.properties.setQuirkIETablePercentWidthScrollbarError(true);
            this.properties.setQuirkCssBackgroundAttachmentUseFixed(true);
            this.properties.setQuirkCssBorderCollapseInside(true);
            this.properties.setQuirkCssBorderCollapseFor0Padding(true);
            if(this.properties.getBrowserVersionMajor()<7){
                this.properties.setProprietaryIEPngAlphaFilterRequired(true);
            }
        }
    }
    private void setMajorMinorVersionByPattern(final String patternString){
        final String userAgent=this.getUserAgentStringLc();
        final Matcher matcher=Pattern.compile(patternString).matcher((CharSequence)userAgent);
        if(matcher.find()){
            this.properties.setBrowserVersionMajor(Integer.parseInt(matcher.group(1)));
            this.properties.setBrowserVersionMinor(Integer.parseInt(matcher.group(2)));
        }
    }
    static{
        log=LoggerFactory.getLogger(WebClientInfo.class);
    }
}
