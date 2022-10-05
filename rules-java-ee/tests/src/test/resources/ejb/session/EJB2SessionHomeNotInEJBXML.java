import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface EJB2SessionHomeNotInEJBXML
        extends javax.ejb.EJBHome {
    public abstract EJB2SessionHomeNotInEJBXML create() throws java.rmi.RemoteException, javax.ejb.CreateException;
}