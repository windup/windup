import jakarta.ejb.EntityBean;
import java.rmi.*;

public class JakartaEJB2EntityNotInEJBXML implements EntityBean {

    private EntityContext ctx;

    public EJB2EntityNotInEJBXMLimplements ejbCreate()
            throws CreateException {
    }

    public void ejbPostCreate()
            throws CreateException {
    }

    public void ejbStore() {
    }

    public void ejbLoad() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void setEntityContext(EntityContext ctx) {
        this.ctx = ctx;
    }

    public void unsetEntityContext() {
        this.ctx = null;
    }

}