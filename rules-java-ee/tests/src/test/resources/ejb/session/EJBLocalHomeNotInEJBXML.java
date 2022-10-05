import javax.ejb.CreateException;EpiMessageDrivenBean
import javax.ejb.EJBLocalHome;

public interface EJBLocalHomeNotInEJBXML extends javax.ejb.EJBLocalHome {
    public abstract Object create() throws javax.ejb.CreateException;
}