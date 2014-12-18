package com.acme.anvil;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.time.DateUtils;
import java.util.Date;
import weblogic.servlet.security.ServletAuthentication;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import weblogic.i18n.logging.NonCatalogLogger;
import javax.servlet.Filter;

public class AuthenticateFilter implements Filter{
    private NonCatalogLogger ncl;
    public AuthenticateFilter(){
        super();
        this.ncl=new NonCatalogLogger("AuthenticateFilter");
    }
    public void destroy(){
        this.ncl.debug("AuthenticateFilter destroy.");
    }
    public void doFilter(final ServletRequest req,final ServletResponse resp,final FilterChain chain) throws IOException,ServletException{
        final HttpServletRequest request=(HttpServletRequest)req;
        final HttpSession session=request.getSession();
        this.ncl.debug("AuthenticateFilter doFilter.");
        if(req.getAttribute("cancelSession")!=null){
            this.ncl.info("Cancelled session due to session timeout.");
            ServletAuthentication.invalidateAll(request);
        }
        else if(session!=null){
            final Date fiveMinutesAgo=DateUtils.addMinutes(new Date(),-5);
            final Date timeLastAccessed=new Date(session.getLastAccessedTime());
            if(timeLastAccessed.before(fiveMinutesAgo)){
                session.invalidate();
                ServletAuthentication.invalidateAll(request);
            }
        }
    }
    public void init(final FilterConfig config) throws ServletException{
        this.ncl.debug("AuthenticateFilter init.");
    }
}
