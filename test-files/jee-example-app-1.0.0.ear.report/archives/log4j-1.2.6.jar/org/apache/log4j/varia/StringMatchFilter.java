package org.apache.log4j.varia;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.Filter;

public class StringMatchFilter extends Filter{
    public static final String STRING_TO_MATCH_OPTION="StringToMatch";
    public static final String ACCEPT_ON_MATCH_OPTION="AcceptOnMatch";
    boolean acceptOnMatch;
    String stringToMatch;
    public StringMatchFilter(){
        super();
        this.acceptOnMatch=true;
    }
    public String[] getOptionStrings(){
        return new String[] { "StringToMatch","AcceptOnMatch" };
    }
    public void setOption(final String key,final String value){
        if(key.equalsIgnoreCase("StringToMatch")){
            this.stringToMatch=value;
        }
        else if(key.equalsIgnoreCase("AcceptOnMatch")){
            this.acceptOnMatch=OptionConverter.toBoolean(value,this.acceptOnMatch);
        }
    }
    public void setStringToMatch(final String s){
        this.stringToMatch=s;
    }
    public String getStringToMatch(){
        return this.stringToMatch;
    }
    public void setAcceptOnMatch(final boolean acceptOnMatch){
        this.acceptOnMatch=acceptOnMatch;
    }
    public boolean getAcceptOnMatch(){
        return this.acceptOnMatch;
    }
    public int decide(final LoggingEvent event){
        final String msg=event.getRenderedMessage();
        if(msg==null||this.stringToMatch==null){
            return 0;
        }
        if(msg.indexOf(this.stringToMatch)==-1){
            return 0;
        }
        if(this.acceptOnMatch){
            return 1;
        }
        return -1;
    }
}
