import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;
import javax.ejb.EJBObject;

public interface EJB2RemoteInterfaceNotInEJBXML extends javax.ejb.EJBObject {
    public abstract java.util.ArrayList doStuff(java.lang.String myString) throws java.rmi.RemoteException;
}