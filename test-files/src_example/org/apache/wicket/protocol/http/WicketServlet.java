package org.apache.wicket.protocol.http;

import javax.servlet.http.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.io.*;
import java.io.*;
import javax.servlet.*;
import java.util.*;
import org.slf4j.*;

public class WicketServlet extends HttpServlet{
    private static final long serialVersionUID=1L;
    private static final Logger log;
    protected transient WicketFilter wicketFilter;
    public final void doGet(final HttpServletRequest servletRequest,final HttpServletResponse servletResponse) throws ServletException,IOException{
        if(!this.wicketFilter.processRequest(servletRequest,servletResponse,null)){
            this.fallback(servletRequest,servletResponse);
        }
    }
    public final void doPost(final HttpServletRequest servletRequest,final HttpServletResponse servletResponse) throws ServletException,IOException{
        if(!this.wicketFilter.processRequest(servletRequest,servletResponse,null)){
            this.fallback(servletRequest,servletResponse);
        }
    }
    private static String getURL(final HttpServletRequest httpServletRequest){
        String url=httpServletRequest.getServletPath();
        final String pathInfo=httpServletRequest.getPathInfo();
        if(pathInfo!=null){
            url+=pathInfo;
        }
        final String queryString=httpServletRequest.getQueryString();
        if(queryString!=null){
            url=url+"?"+queryString;
        }
        if(url.length()>0&&url.charAt(0)=='/'){
            url=url.substring(1);
        }
        return url;
    }
    private void fallback(final HttpServletRequest request,final HttpServletResponse response) throws IOException{
        String url=getURL(request);
        if(url.indexOf(63)!=-1){
            url=Strings.beforeFirst(url,'?');
        }
        if((url.length()>0&&url.charAt(0)!='/')||url.length()==0){
            url='/'+url;
        }
        final InputStream stream=this.getServletContext().getResourceAsStream(url);
        final String mimeType=this.getServletContext().getMimeType(url);
        if(stream==null){
            if(response.isCommitted()){
                response.setStatus(404);
            }
            else{
                response.sendError(404);
            }
        }
        else{
            if(mimeType!=null){
                response.setContentType(mimeType);
            }
            try{
                Streams.copy(stream,(OutputStream)response.getOutputStream());
            }
            finally{
                stream.close();
            }
        }
    }
    public void init() throws ServletException{
        (this.wicketFilter=this.newWicketFilter()).init(true,new FilterConfig(){
            public ServletContext getServletContext(){
                return WicketServlet.this.getServletContext();
            }
            public Enumeration<String> getInitParameterNames(){
                return (Enumeration<String>)WicketServlet.this.getInitParameterNames();
            }
            public String getInitParameter(final String name){
                return WicketServlet.this.getInitParameter(name);
            }
            public String getFilterName(){
                return WicketServlet.this.getServletName();
            }
        });
    }
    protected WicketFilter newWicketFilter(){
        return new WicketFilter();
    }
    public void destroy(){
        this.wicketFilter.destroy();
        this.wicketFilter=null;
    }
    static{
        log=LoggerFactory.getLogger(WicketServlet.class);
    }
}
