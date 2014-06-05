package org.apache.wicket.util.resource.locator;

import java.util.*;
import org.apache.wicket.util.string.*;

public class ExtensionResourceNameIterator implements Iterator<String>{
    private final String[] extensions;
    private int index;
    public ExtensionResourceNameIterator(final String extension,final char separatorChar){
        super();
        String[] extensions=Strings.split(extension,separatorChar);
        if(extensions.length==0){
            extensions=new String[] { "" };
        }
        this.extensions=extensions;
        this.index=0;
    }
    public boolean hasNext(){
        return this.index<this.extensions.length;
    }
    public String next(){
        ++this.index;
        return this.getExtension();
    }
    public final String getExtension(){
        final String ext=this.extensions[this.index-1];
        return ext.startsWith(".")?ext.substring(1):ext;
    }
    public void remove(){
    }
}
