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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.time.DateUtils;

import org.jboss.logging.Logger;
//import weblogic.servlet.security.ServletAuthentication;
// Use HttpServletRequest.login() or @ServletSecurity
// See https://docs.jboss.org/author/display/AS72/How+do+I+migrate+my+application+from+WebLogic+to+AS+7#HowdoImigratemyapplicationfromWebLogictoAS7-ProgrammaticLogin

public class AuthenticateFilter implements Filter {


    private static final Logger log = Logger.getLogger(AuthenticateFilter.class);
	
	public void destroy() {
		log.debug("AuthenticateFilter destroy.");
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
	    HttpServletRequest request = (HttpServletRequest)req;
	    HttpSession session = request.getSession();
	    
		log.debug("AuthenticateFilter doFilter.");
		if(req.getAttribute("cancelSession") != null) {
			log.info("Cancelled session due to session timeout.");
            ((HttpServletRequest)req).logout();
		}
		else if(session != null) {
			Date fiveMinutesAgo = DateUtils.addMinutes(new Date(), -5);
			// Check that the time the session was last accessed was after 5 minutes ago.
			Date timeLastAccessed = new Date(session.getLastAccessedTime());
			
			if(timeLastAccessed.before(fiveMinutesAgo)) {
				session.invalidate();
				// Make the user log back in.
				((HttpServletRequest)req).logout();
			}
		}
		
	}

	public void init(FilterConfig config) throws ServletException {
		log.debug("AuthenticateFilter init.");
	}

}
