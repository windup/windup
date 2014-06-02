package org.apache.wicket.authorization;

import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.string.*;

public class Action extends EnumeratedType{
    public static final String RENDER="RENDER";
    public static final String ENABLE="ENABLE";
    private static final long serialVersionUID=-1L;
    private final String name;
    public Action(final String name){
        super(name);
        if(Strings.isEmpty((CharSequence)name)){
            throw new IllegalArgumentException("Name argument may not be null, whitespace or the empty string");
        }
        this.name=name;
    }
    public String getName(){
        return this.name;
    }
}
