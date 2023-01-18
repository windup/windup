import jakarta.ejb.CreateException;
import jakarta.ejb.EJBLocalHome;

public interface JakartaEJBLocalHomeNotInEJBXML extends jakarta.ejb.EJBLocalHome {
    public abstract Object create() throws jakarta.ejb.CreateException;
}