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

import org.jboss.logging.Logger;


public class LoginFilter implements Filter {

	private static final Logger log = Logger.getLogger(LoginFilter.class);
	

	public void destroy() {
		log.debug("LoginFilter destroy.");
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)resp;
		
	    HttpSession session = request.getSession();
	    //...
	}

	public void init(FilterConfig config) throws ServletException {
		log.debug("LoginFilter init.");
	}
}
