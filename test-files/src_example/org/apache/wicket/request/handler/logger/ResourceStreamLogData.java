package org.apache.wicket.request.handler.logger;

import org.apache.wicket.util.resource.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.handler.resource.*;
import java.util.*;

public class ResourceStreamLogData extends ResourceLogData{
    private static final long serialVersionUID=1L;
    private final Class<? extends IResourceStream> resourceStreamClass;
    private final ContentDisposition contentDisposition;
    private final String contentType;
    public ResourceStreamLogData(final ResourceStreamRequestHandler streamHandler){
        super(streamHandler.getFileName(),null,null,null);
        this.contentDisposition=streamHandler.getContentDisposition();
        this.resourceStreamClass=null;
        this.contentType=null;
    }
    public ResourceStreamLogData(final ResourceStreamRequestHandler streamHandler,final IResourceStream stream){
        super(streamHandler.getFileName(),stream.getLocale(),stream.getStyle(),stream.getVariation());
        this.contentDisposition=streamHandler.getContentDisposition();
        this.resourceStreamClass=(Class<? extends IResourceStream>)stream.getClass();
        this.contentType=stream.getContentType();
    }
    public final Class<? extends IResourceStream> getResourceStreamClass(){
        return this.resourceStreamClass;
    }
    public final ContentDisposition getContentDisposition(){
        return this.contentDisposition;
    }
    public final String getContentType(){
        return this.contentType;
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder("{");
        this.fillToString(sb);
        sb.append(",contentDisposition=");
        sb.append(this.getContentDisposition());
        if(this.getResourceStreamClass()!=null){
            sb.append(",resourceStreamClass=");
            sb.append(this.getResourceStreamClass().getName());
            sb.append(",contentType=");
            sb.append(this.getContentType());
        }
        sb.append("}");
        return sb.toString();
    }
}
