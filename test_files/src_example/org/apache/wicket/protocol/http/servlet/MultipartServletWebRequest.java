package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.protocol.http.*;
import javax.servlet.http.*;
import org.apache.wicket.util.upload.*;
import java.util.*;
import org.apache.wicket.request.http.*;
import org.apache.wicket.request.*;

public abstract class MultipartServletWebRequest extends ServletWebRequest implements IMultipartWebRequest{
    public MultipartServletWebRequest(final HttpServletRequest httpServletRequest,final String filterPrefix){
        super(httpServletRequest,filterPrefix);
    }
    public MultipartServletWebRequest(final HttpServletRequest httpServletRequest,final String filterPrefix,final Url url){
        super(httpServletRequest,filterPrefix,url);
    }
    public ServletWebRequest cloneWithUrl(final Url url){
        return new MultipartServletWebRequest(this.getContainerRequest(),this.getFilterPrefix(),url){
            public List<FileItem> getFile(final String fieldName){
                return MultipartServletWebRequest.this.getFile(fieldName);
            }
            public Map<String,List<FileItem>> getFiles(){
                return MultipartServletWebRequest.this.getFiles();
            }
            public IRequestParameters getPostParameters(){
                return MultipartServletWebRequest.this.getPostParameters();
            }
        };
    }
}
