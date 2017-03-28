package org.apache.geronimo.daytrader.javaee7;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import sample.Service;

public class NonPortableJNDIReference extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();
        out.println("JNDI=" + lookupService());
    }

    private Service lookupService() {
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put( Context.PROVIDER_URL, "t3://localhost:7001" );
            env.put( Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory" );
            env.put( Context.SECURITY_PRINCIPAL, "weblogic" );
            env.put( Context.SECURITY_CREDENTIALS, "weblogic" );
            Context context = new InitialContext( env );
            Service service = (Service)context.lookup("sample.Service#" + Service.class.getName());
            return service;
        } catch (NamingExceptin e) {
            return null;
        }
    }
}