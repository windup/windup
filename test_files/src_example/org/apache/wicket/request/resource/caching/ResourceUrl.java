package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.mapper.parameter.*;

public class ResourceUrl{
    private String fileName;
    private INamedParameters parameters;
    public ResourceUrl(final String fileName,final INamedParameters urlParameters){
        super();
        this.fileName=fileName;
        this.parameters=urlParameters;
    }
    public String getFileName(){
        return this.fileName;
    }
    public void setFileName(final String fileName){
        this.fileName=fileName;
    }
    public INamedParameters getParameters(){
        return this.parameters;
    }
    public String toString(){
        return "Name: "+this.fileName+"\n\tParameters: "+this.parameters;
    }
}
