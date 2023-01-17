import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

public class JakartaEJB2SessionBeanNotInEJBXML implements jakarta.ejb.SessionBean {
    public void setSessionContext(jakarta.ejb.SessionContext sessioncontext)
            throws jakarta.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbCreate() {
    }

    public void ejbRemove()
            throws jakarta.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbActivate()
            throws jakarta.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbPassivate()
            throws jakarta.ejb.EJBException, java.rmi.RemoteException {
    }
}