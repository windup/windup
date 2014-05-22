package com.acme.anvil;

import java.io.IOException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.acme.anvil.service.ItemLookupLocal;
import com.acme.anvil.service.ItemLookupLocalHome;

public class AnvilWebServlet extends HttpServlet {

	private static final Logger LOG = Logger.getLogger(AnvilWebServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		InitialContext ic;
		ItemLookupLocalHome lh;
		ItemLookupLocal local;
		try {
			ic = new InitialContext();
			lh  = (ItemLookupLocalHome)ic.lookup("ejb/ItemLookupLocal");
			local = lh.create();
			
			String itemId = req.getParameter("id");
			if(StringUtils.isNotBlank(itemId)) {
				Long id = Long.parseLong(itemId);
				local.lookupItem(id);
			}
		} catch (EJBException e) {
			LOG.error("Exception creating EJB.", e);
		} catch (CreateException e) {
			LOG.error("Exception creating EJB.", e);
		} catch (NamingException e) {
			LOG.error("Exception looking up EJB LocalHome.", e);
		}
		
		
		
	}
}
