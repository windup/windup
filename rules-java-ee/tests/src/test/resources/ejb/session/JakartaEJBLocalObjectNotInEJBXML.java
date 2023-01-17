import jakarta.ejb.EJBLocalObject;

public interface JakartaEJBLocalObjectNotInEJBXML extends jakarta.ejb.EJBLocalObject {
    public abstract Object createStuff();
}