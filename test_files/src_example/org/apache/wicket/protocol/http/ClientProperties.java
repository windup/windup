package org.apache.wicket.protocol.http;

import org.apache.wicket.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.cycle.*;
import org.apache.wicket.request.http.*;
import java.util.*;
import javax.servlet.http.*;
import java.lang.reflect.*;

public class ClientProperties implements IClusterable{
    private static final long serialVersionUID=1L;
    private int browserHeight;
    private boolean browserInternetExplorer;
    private boolean browserKonqueror;
    private boolean browserMozilla;
    private boolean browserMozillaFirefox;
    private boolean browserOpera;
    private boolean browserSafari;
    private boolean browserChrome;
    private int browserVersionMajor;
    private int browserVersionMinor;
    private int browserWidth;
    private boolean cookiesEnabled;
    private boolean javaEnabled;
    private String navigatorAppCodeName;
    private String navigatorAppName;
    private String navigatorAppVersion;
    private String navigatorLanguage;
    private String navigatorPlatform;
    private String navigatorUserAgent;
    private boolean proprietaryIECssExpressionsSupported;
    private boolean proprietaryIEPngAlphaFilterRequired;
    private boolean quirkCssBackgroundAttachmentUseFixed;
    private boolean quirkCssBorderCollapseFor0Padding;
    private boolean quirkCssBorderCollapseInside;
    private boolean quirkCssPositioningOneSideOnly;
    private boolean quirkIERepaint;
    private boolean quirkIESelectListDomUpdate;
    private boolean quirkIESelectPercentWidth;
    private boolean quirkIESelectZIndex;
    private boolean quirkIETablePercentWidthScrollbarError;
    private boolean quirkIETextareaNewlineObliteration;
    private boolean quirkMozillaPerformanceLargeDomRemove;
    private boolean quirkMozillaTextInputRepaint;
    private String remoteAddress;
    private int screenColorDepth;
    private int screenHeight;
    private int screenWidth;
    private TimeZone timeZone;
    private String utcDSTOffset;
    private String utcOffset;
    private String hostname;
    public ClientProperties(){
        super();
        this.browserHeight=-1;
        this.browserVersionMajor=-1;
        this.browserVersionMinor=-1;
        this.browserWidth=-1;
        this.screenColorDepth=-1;
        this.screenHeight=-1;
        this.screenWidth=-1;
    }
    public int getBrowserHeight(){
        return this.browserHeight;
    }
    public int getBrowserVersionMajor(){
        return this.browserVersionMajor;
    }
    public int getBrowserVersionMinor(){
        return this.browserVersionMinor;
    }
    public int getBrowserWidth(){
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
    public String getNavigatorLanguage(){
        return this.navigatorLanguage;
    }
    public String getNavigatorPlatform(){
        return this.navigatorPlatform;
    }
    public String getNavigatorUserAgent(){
        return this.navigatorUserAgent;
    }
    public String getRemoteAddress(){
        return this.remoteAddress;
    }
    public String getHostname(){
        return this.hostname;
    }
    public int getScreenColorDepth(){
        return this.screenColorDepth;
    }
    public int getScreenHeight(){
        return this.screenHeight;
    }
    public int getScreenWidth(){
        return this.screenWidth;
    }
    public TimeZone getTimeZone(){
        if(this.timeZone==null){
            String utc=this.getUtcOffset();
            if(utc!=null){
                int dotPos=utc.indexOf(46);
                if(dotPos>=0){
                    String hours=utc.substring(0,dotPos);
                    final String hourPart=utc.substring(dotPos+1);
                    if(hours.startsWith("+")){
                        hours=hours.substring(1);
                    }
                    final int offsetHours=Integer.parseInt(hours);
                    final int offsetMins=(int)(Double.parseDouble(hourPart)*6.0);
                    final AppendingStringBuffer sb=new AppendingStringBuffer((CharSequence)"GMT");
                    sb.append((offsetHours>0)?"+":"-");
                    sb.append(Math.abs(offsetHours));
                    sb.append(":");
                    if(offsetMins<10){
                        sb.append("0");
                    }
                    sb.append(offsetMins);
                    this.timeZone=TimeZone.getTimeZone(sb.toString());
                }
                else{
                    final int offset=Integer.parseInt(utc);
                    if(offset<0){
                        utc=utc.substring(1);
                    }
                    this.timeZone=TimeZone.getTimeZone("GMT"+((offset>0)?"+":"-")+utc);
                }
                String dstOffset=this.getUtcDSTOffset();
                if(this.timeZone!=null&&dstOffset!=null){
                    TimeZone dstTimeZone=null;
                    dotPos=dstOffset.indexOf(46);
                    if(dotPos>=0){
                        String hours2=dstOffset.substring(0,dotPos);
                        final String hourPart2=dstOffset.substring(dotPos+1);
                        if(hours2.startsWith("+")){
                            hours2=hours2.substring(1);
                        }
                        final int offsetHours2=Integer.parseInt(hours2);
                        final int offsetMins2=(int)(Double.parseDouble(hourPart2)*6.0);
                        final AppendingStringBuffer sb2=new AppendingStringBuffer((CharSequence)"GMT");
                        sb2.append((offsetHours2>0)?"+":"-");
                        sb2.append(Math.abs(offsetHours2));
                        sb2.append(":");
                        if(offsetMins2<10){
                            sb2.append("0");
                        }
                        sb2.append(offsetMins2);
                        dstTimeZone=TimeZone.getTimeZone(sb2.toString());
                    }
                    else{
                        final int offset2=Integer.parseInt(dstOffset);
                        if(offset2<0){
                            dstOffset=dstOffset.substring(1);
                        }
                        dstTimeZone=TimeZone.getTimeZone("GMT"+((offset2>0)?"+":"-")+dstOffset);
                    }
                    if(dstTimeZone!=null&&dstTimeZone.getRawOffset()!=this.timeZone.getRawOffset()){
                        final int dstSaving=dstTimeZone.getRawOffset()-this.timeZone.getRawOffset();
                        final String[] arr$;
                        final String[] availableIDs=arr$=TimeZone.getAvailableIDs(this.timeZone.getRawOffset());
                        for(final String availableID : arr$){
                            final TimeZone zone=TimeZone.getTimeZone(availableID);
                            if(zone.getDSTSavings()==dstSaving){
                                this.timeZone=zone;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return this.timeZone;
    }
    public String getUtcDSTOffset(){
        return this.utcDSTOffset;
    }
    public String getUtcOffset(){
        return this.utcOffset;
    }
    public boolean isBrowserInternetExplorer(){
        return this.browserInternetExplorer;
    }
    public boolean isBrowserKonqueror(){
        return this.browserKonqueror;
    }
    public boolean isBrowserMozilla(){
        return this.browserMozilla;
    }
    public boolean isBrowserMozillaFirefox(){
        return this.browserMozillaFirefox;
    }
    public boolean isBrowserOpera(){
        return this.browserOpera;
    }
    public boolean isBrowserSafari(){
        return this.browserSafari;
    }
    public boolean isBrowserChrome(){
        return this.browserChrome;
    }
    public boolean isCookiesEnabled(){
        if(!this.cookiesEnabled&&RequestCycle.get()!=null){
            final Collection<Cookie> cookies=(Collection<Cookie>)((WebRequest)RequestCycle.get().getRequest()).getCookies();
            this.cookiesEnabled=(cookies!=null&&cookies.size()>0);
        }
        return this.cookiesEnabled;
    }
    public boolean isJavaEnabled(){
        return this.javaEnabled;
    }
    public boolean isProprietaryIECssExpressionsSupported(){
        return this.proprietaryIECssExpressionsSupported;
    }
    public boolean isProprietaryIEPngAlphaFilterRequired(){
        return this.proprietaryIEPngAlphaFilterRequired;
    }
    public boolean isQuirkCssBackgroundAttachmentUseFixed(){
        return this.quirkCssBackgroundAttachmentUseFixed;
    }
    public boolean isQuirkCssBorderCollapseFor0Padding(){
        return this.quirkCssBorderCollapseFor0Padding;
    }
    public boolean isQuirkCssBorderCollapseInside(){
        return this.quirkCssBorderCollapseInside;
    }
    public boolean isQuirkCssPositioningOneSideOnly(){
        return this.quirkCssPositioningOneSideOnly;
    }
    public boolean isQuirkIERepaint(){
        return this.quirkIERepaint;
    }
    public boolean isQuirkIESelectListDomUpdate(){
        return this.quirkIESelectListDomUpdate;
    }
    public boolean isQuirkIESelectPercentWidth(){
        return this.quirkIESelectPercentWidth;
    }
    public boolean isQuirkIESelectZIndex(){
        return this.quirkIESelectZIndex;
    }
    public boolean isQuirkIETablePercentWidthScrollbarError(){
        return this.quirkIETablePercentWidthScrollbarError;
    }
    public boolean isQuirkIETextareaNewlineObliteration(){
        return this.quirkIETextareaNewlineObliteration;
    }
    public boolean isQuirkMozillaPerformanceLargeDomRemove(){
        return this.quirkMozillaPerformanceLargeDomRemove;
    }
    public boolean isQuirkMozillaTextInputRepaint(){
        return this.quirkMozillaTextInputRepaint;
    }
    public void setBrowserHeight(final int browserHeight){
        this.browserHeight=browserHeight;
    }
    public void setBrowserInternetExplorer(final boolean browserInternetExplorer){
        this.browserInternetExplorer=browserInternetExplorer;
    }
    public void setBrowserKonqueror(final boolean browserKonqueror){
        this.browserKonqueror=browserKonqueror;
    }
    public void setBrowserMozilla(final boolean browserMozilla){
        this.browserMozilla=browserMozilla;
    }
    public void setBrowserMozillaFirefox(final boolean browserMozillaFirefox){
        this.browserMozillaFirefox=browserMozillaFirefox;
    }
    public void setBrowserOpera(final boolean browserOpera){
        this.browserOpera=browserOpera;
    }
    public void setBrowserSafari(final boolean browserSafari){
        this.browserSafari=browserSafari;
    }
    public void setBrowserChrome(final boolean browserChrome){
        this.browserChrome=browserChrome;
    }
    public void setBrowserVersionMajor(final int browserVersionMajor){
        this.browserVersionMajor=browserVersionMajor;
    }
    public void setBrowserVersionMinor(final int browserVersionMinor){
        this.browserVersionMinor=browserVersionMinor;
    }
    public void setBrowserWidth(final int browserWidth){
        this.browserWidth=browserWidth;
    }
    public void setCookiesEnabled(final boolean cookiesEnabled){
        this.cookiesEnabled=cookiesEnabled;
    }
    public void setJavaEnabled(final boolean navigatorJavaEnabled){
        this.javaEnabled=navigatorJavaEnabled;
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
    public void setNavigatorLanguage(final String navigatorLanguage){
        this.navigatorLanguage=navigatorLanguage;
    }
    public void setNavigatorPlatform(final String navigatorPlatform){
        this.navigatorPlatform=navigatorPlatform;
    }
    public void setNavigatorUserAgent(final String navigatorUserAgent){
        this.navigatorUserAgent=navigatorUserAgent;
    }
    public void setProprietaryIECssExpressionsSupported(final boolean proprietaryIECssExpressionsSupported){
        this.proprietaryIECssExpressionsSupported=proprietaryIECssExpressionsSupported;
    }
    public void setProprietaryIEPngAlphaFilterRequired(final boolean proprietaryIEPngAlphaFilterRequired){
        this.proprietaryIEPngAlphaFilterRequired=proprietaryIEPngAlphaFilterRequired;
    }
    public void setQuirkCssBackgroundAttachmentUseFixed(final boolean quirkCssBackgroundAttachmentUseFixed){
        this.quirkCssBackgroundAttachmentUseFixed=quirkCssBackgroundAttachmentUseFixed;
    }
    public void setQuirkCssBorderCollapseFor0Padding(final boolean quirkCssBorderCollapseFor0Padding){
        this.quirkCssBorderCollapseFor0Padding=quirkCssBorderCollapseFor0Padding;
    }
    public void setQuirkCssBorderCollapseInside(final boolean quirkCssBorderCollapseInside){
        this.quirkCssBorderCollapseInside=quirkCssBorderCollapseInside;
    }
    public void setQuirkCssPositioningOneSideOnly(final boolean quirkCssPositioningOneSideOnly){
        this.quirkCssPositioningOneSideOnly=quirkCssPositioningOneSideOnly;
    }
    public void setQuirkIERepaint(final boolean quirkIERepaint){
        this.quirkIERepaint=quirkIERepaint;
    }
    public void setQuirkIESelectListDomUpdate(final boolean quirkIESelectListDomUpdate){
        this.quirkIESelectListDomUpdate=quirkIESelectListDomUpdate;
    }
    public void setQuirkIESelectPercentWidth(final boolean quirkIESelectPercentWidth){
        this.quirkIESelectPercentWidth=quirkIESelectPercentWidth;
    }
    public void setQuirkIESelectZIndex(final boolean quirkIESelectZIndex){
        this.quirkIESelectZIndex=quirkIESelectZIndex;
    }
    public void setQuirkIETablePercentWidthScrollbarError(final boolean quirkIETablePercentWidthScrollbarError){
        this.quirkIETablePercentWidthScrollbarError=quirkIETablePercentWidthScrollbarError;
    }
    public void setQuirkIETextareaNewlineObliteration(final boolean quirkIETextareaNewlineObliteration){
        this.quirkIETextareaNewlineObliteration=quirkIETextareaNewlineObliteration;
    }
    public void setQuirkMozillaPerformanceLargeDomRemove(final boolean quirkMozillaPerformanceLargeDomRemove){
        this.quirkMozillaPerformanceLargeDomRemove=quirkMozillaPerformanceLargeDomRemove;
    }
    public void setQuirkMozillaTextInputRepaint(final boolean quirkMozillaTextInputRepaint){
        this.quirkMozillaTextInputRepaint=quirkMozillaTextInputRepaint;
    }
    public void setRemoteAddress(final String remoteAddress){
        this.remoteAddress=remoteAddress;
    }
    public void setHostname(final String hostname){
        this.hostname=hostname;
    }
    public void setScreenColorDepth(final int screenColorDepth){
        this.screenColorDepth=screenColorDepth;
    }
    public void setScreenHeight(final int screenHeight){
        this.screenHeight=screenHeight;
    }
    public void setScreenWidth(final int screenWidth){
        this.screenWidth=screenWidth;
    }
    public void setTimeZone(final TimeZone timeZone){
        this.timeZone=timeZone;
    }
    public void setUtcDSTOffset(final String utcDSTOffset){
        this.utcDSTOffset=utcDSTOffset;
    }
    public void setUtcOffset(final String utcOffset){
        this.utcOffset=utcOffset;
    }
    public String toString(){
        final StringBuilder b=new StringBuilder();
        final Field[] arr$;
        final Field[] fields=arr$=ClientProperties.class.getDeclaredFields();
        for(final Field field : arr$){
            if(!field.getName().equals("serialVersionUID")&&!field.getName().startsWith("class$")&&!field.getName().startsWith("timeZone")){
                field.setAccessible(true);
                Object value=null;
                try{
                    value=field.get(this);
                }
                catch(IllegalArgumentException e){
                    throw new RuntimeException((Throwable)e);
                }
                catch(IllegalAccessException e2){
                    throw new RuntimeException((Throwable)e2);
                }
                if(field.getType().equals(Integer.TYPE)&&(int)value==-1){
                    value=null;
                }
                if(value!=null){
                    b.append(field.getName());
                    b.append("=");
                    b.append(value);
                    b.append("\n");
                }
            }
        }
        return b.toString();
    }
}
