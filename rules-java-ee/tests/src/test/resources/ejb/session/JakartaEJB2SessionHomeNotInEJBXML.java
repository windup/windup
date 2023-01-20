import java.rmi.RemoteException;
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;

public interface JakartaEJB2SessionHomeNotInEJBXML
        extends EJBHome {
    public abstract EJB2SessionHomeNotInEJBXML create() throws java.rmi.RemoteException, jakarta.ejb.CreateException;
}