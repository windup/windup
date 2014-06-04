package org.apache.wicket.util.resource;

import java.util.*;
import org.apache.wicket.*;
import java.io.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.time.*;

public class PackageResourceStream extends AbstractResourceStream{
    private static final long serialVersionUID=1L;
    private IResourceStream resourceStream;
    public PackageResourceStream(final Class<?> scope,final String path){
        super();
        final String absolutePath=Packages.absolutePath((Class)scope,path);
        this.resourceStream=Application.get().getResourceSettings().getResourceStreamLocator().locate(scope,absolutePath,null,null,null,null,false);
        if(this.resourceStream==null){
            throw new WicketRuntimeException("Cannot find resource with "+scope.getName()+" and path "+path);
        }
    }
    public void close() throws IOException{
        this.resourceStream.close();
    }
    public String getContentType(){
        return this.resourceStream.getContentType();
    }
    public InputStream getInputStream() throws ResourceStreamNotFoundException{
        return this.resourceStream.getInputStream();
    }
    public Bytes length(){
        return this.resourceStream.length();
    }
    public Time lastModifiedTime(){
        return this.resourceStream.lastModifiedTime();
    }
}
