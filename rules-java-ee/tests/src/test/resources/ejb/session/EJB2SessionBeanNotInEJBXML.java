import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class EJB2SessionBeanNotInEJBXML implements javax.ejb.SessionBean {
    public void setSessionContext(javax.ejb.SessionContext sessioncontext)
            throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbCreate() {
    }

    public void ejbRemove()
            throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbActivate()
            throws javax.ejb.EJBException, java.rmi.RemoteException {
    }

    public void ejbPassivate()
            throws javax.ejb.EJBException, java.rmi.RemoteException {
    }
}