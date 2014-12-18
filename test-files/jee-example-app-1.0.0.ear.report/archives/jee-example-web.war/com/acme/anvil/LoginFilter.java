package com.acme.anvil;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import weblogic.i18n.logging.NonCatalogLogger;
import javax.servlet.Filter;

public class LoginFilter implements Filter{
    private NonCatalogLogger ncl;
    public LoginFilter(){
        super();
        this.ncl=new NonCatalogLogger("LoginFilter");
    }
    public void destroy(){
        this.ncl.debug("LoginFilter destroy.");
    }
    public void doFilter(final ServletRequest req,final ServletResponse resp,final FilterChain chain) throws IOException,ServletException{
        final HttpServletRequest request=(HttpServletRequest)req;
        final HttpServletResponse response=(HttpServletResponse)resp;
        final HttpSession session=request.getSession();
    }
    public void init(final FilterConfig config) throws ServletException{
        this.ncl.debug("LoginFilter init.");
    }
}
