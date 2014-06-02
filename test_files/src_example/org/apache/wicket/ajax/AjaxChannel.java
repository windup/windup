package org.apache.wicket.ajax;

import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;

public class AjaxChannel implements IClusterable{
    private static final long serialVersionUID=1L;
    private final String name;
    private final Type type;
    public AjaxChannel(final String name){
        this(name,Type.QUEUE);
    }
    public AjaxChannel(final String name,final Type type){
        super();
        this.name=(String)Args.notNull((Object)name,"name");
        this.type=(Type)Args.notNull((Object)type,"type");
    }
    public String getName(){
        return this.name;
    }
    public Type getType(){
        return this.type;
    }
    String getChannelName(){
        return this.toString();
    }
    public String toString(){
        return String.format("%s|%s",new Object[] { this.name,this.getShortType(this.type) });
    }
    private String getShortType(final Type t){
        String shortType=null;
        switch(t){
            case DROP:{
                shortType="d";
                break;
            }
            case ACTIVE:{
                shortType="a";
                break;
            }
            default:{
                shortType="s";
                break;
            }
        }
        return shortType;
    }
    public enum Type{
        QUEUE,DROP,ACTIVE;
    }
}
