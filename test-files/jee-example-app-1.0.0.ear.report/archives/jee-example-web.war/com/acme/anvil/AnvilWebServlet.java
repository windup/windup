package com.acme.anvil;

import java.io.IOException;
import javax.servlet.ServletException;
import com.acme.anvil.service.ItemLookupLocal;
import javax.naming.NamingException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.commons.lang.StringUtils;
import com.acme.anvil.service.ItemLookupLocalHome;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServlet;

public class AnvilWebServlet extends HttpServlet{
    private static final Logger LOG;
    protected void doGet(final HttpServletRequest req,final HttpServletResponse resp) throws ServletException,IOException{
        try{
            final InitialContext ic=new InitialContext();
            final ItemLookupLocalHome lh=(ItemLookupLocalHome)ic.lookup("ejb/ItemLookupLocal");
            final ItemLookupLocal local=lh.create();
            final String itemId=req.getParameter("id");
            if(StringUtils.isNotBlank(itemId)){
                final Long id=Long.parseLong(itemId);
                local.lookupItem((long)id);
            }
        }
        catch(EJBException e){
            AnvilWebServlet.LOG.error((Object)"Exception creating EJB.",(Throwable)e);
        }
        catch(CreateException e2){
            AnvilWebServlet.LOG.error((Object)"Exception creating EJB.",(Throwable)e2);
        }
        catch(NamingException e3){
            AnvilWebServlet.LOG.error((Object)"Exception looking up EJB LocalHome.",(Throwable)e3);
        }
    }
    static{
        LOG=Logger.getLogger((Class)AnvilWebServlet.class);
    }
}
