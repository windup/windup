package com.acme.anvil;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.DateUtils;

import weblogic.i18n.logging.NonCatalogLogger;
import weblogic.servlet.security.ServletAuthentication;

public class LoginFilter implements Filter {

	private NonCatalogLogger ncl = new NonCatalogLogger("LoginFilter");
	

	public void destroy() {
		ncl.debug("LoginFilter destroy.");
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		
	    HttpSession session = request.getSession();
	    //...
	}

	public void init(FilterConfig config) throws ServletException {
		ncl.debug("LoginFilter init.");
	}
}
