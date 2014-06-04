package org.apache.wicket.protocol.http.request;

import org.apache.wicket.util.string.*;
import java.util.*;

enum UserAgent{
    MOZILLA("Opera,AppleWebKit,Konqueror",(List<String>[])new List[] { Arrays.asList(new String[] { "Mozilla","Gecko" }) }),FIREFOX("Opera,AppleWebKit,Konqueror",(List<String>[])new List[] { Arrays.asList(new String[] { "Mozilla","Gecko","Firefox" }) }),INTERNET_EXPLORER("Opera",(List<String>[])new List[] { Arrays.asList(new String[] { "Mozilla","MSIE","Windows" }),Arrays.asList(new String[] { "Mozilla","MSIE","Trident" }),Arrays.asList(new String[] { "Mozilla","MSIE","Mac_PowerPC" }) }),OPERA((List<String>[])new List[] { Arrays.asList(new String[] { "Opera" }) }),CHROME((List<String>[])new List[] { Arrays.asList(new String[] { "Mozilla","Chrome","AppleWebKit","Safari" }) }),SAFARI("Chrome",(List<String>[])new List[] { Arrays.asList(new String[] { "Mozilla","AppleWebKit","Safari" }) }),KONQUEROR((List<String>[])new List[] { Arrays.asList(new String[] { "Konqueror" }) });
    private final String[] notAllowedList;
    private final List<String>[] detectionStrings;
    private UserAgent(final String notAllowed,final List<String>[] detectionStrings){
        this.notAllowedList=Strings.split(notAllowed,',');
        this.detectionStrings=detectionStrings;
    }
    private UserAgent(final List<String>[] detectionStrings){
        this((String)null,detectionStrings);
    }
    public boolean matches(final String userAgent){
        if(userAgent==null){
            return false;
        }
        if(this.notAllowedList!=null){
            for(final String value : this.notAllowedList){
                if(userAgent.contains((CharSequence)value)){
                    return false;
                }
            }
        }
        for(final List<String> detectionGroup : this.detectionStrings){
            boolean groupPassed=true;
            for(final String detectionString : detectionGroup){
                if(!userAgent.contains((CharSequence)detectionString)){
                    groupPassed=false;
                    break;
                }
            }
            if(groupPassed){
                return true;
            }
        }
        return false;
    }
}
