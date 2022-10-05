import javax.ejb.EJBLocalObject;

public interface EJBLocalObjectNotInEJBXML extends javax.ejb.EJBLocalObject {
    public abstract Object createStuff();
}