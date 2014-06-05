package org.apache.wicket.protocol.http.servlet;

import javax.servlet.*;
import org.apache.wicket.protocol.http.*;
import java.io.*;
import javax.servlet.http.*;
import org.apache.wicket.*;
import org.apache.wicket.request.http.*;
import org.slf4j.*;

public class WicketSessionFilter implements Filter{
    private static final Logger logger;
    private String filterName;
    private String sessionKey;
    public void init(final FilterConfig filterConfig) throws ServletException{
        this.filterName=filterConfig.getInitParameter("filterName");
        if(this.filterName==null){
            throw new ServletException("you must provide init parameter 'filterName if you want to use "+this.getClass().getName());
        }
        WicketSessionFilter.logger.debug("filterName/application key set to {}",this.filterName);
    }
    public void doFilter(final ServletRequest request,final ServletResponse response,final FilterChain chain) throws IOException,ServletException{
        try{
            final WebApplication application=this.bindApplication();
            this.bindSession(request,application);
            chain.doFilter(request,response);
        }
        finally{
            this.cleanupBoundApplicationAndSession();
        }
    }
    private void cleanupBoundApplicationAndSession(){
        ThreadContext.detach();
    }
    private void bindSession(final ServletRequest request,final WebApplication application){
        final HttpSession httpSession=((HttpServletRequest)request).getSession(false);
        final Session session=this.getSession(httpSession,application);
        if(session==null){
            if(WicketSessionFilter.logger.isDebugEnabled()){
                WicketSessionFilter.logger.debug("could not set Wicket session: key "+this.sessionKey+" not found in http session for "+((HttpServletRequest)request).getContextPath()+","+request.getServerName()+", or http session does not exist");
            }
        }
        else{
            ThreadContext.setSession(session);
        }
    }
    private WebApplication bindApplication(){
        final WebApplication application=(WebApplication)Application.get(this.filterName);
        if(application==null){
            throw new IllegalStateException("Could not find wicket application mapped to filter: "+this.filterName+". Make sure you set filterName attribute to the name of the wicket filter "+"for the wicket application whose session you want to access.");
        }
        ThreadContext.setApplication(application);
        return application;
    }
    private Session getSession(final HttpSession session,final WebApplication application){
        if(session!=null){
            if(this.sessionKey==null){
                this.sessionKey=application.getSessionAttributePrefix(null,this.filterName)+"session";
                WicketSessionFilter.logger.debug("will use {} as the session key to get the Wicket session",this.sessionKey);
            }
            return (Session)session.getAttribute(this.sessionKey);
        }
        return null;
    }
    public void destroy(){
    }
    static{
        logger=LoggerFactory.getLogger(WicketSessionFilter.class);
    }
}
