import java.rmi.RemoteException;
import java.util.ArrayList;

public interface JakartaEJB2RemoteInterfaceNotInEJBXML extends jakarta.ejb.EJBObject {
    public abstract ArrayList doStuff(String myString) throws RemoteException;
}