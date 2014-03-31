package samples.ejb;

import javax.ejb.EJBLocalHome;

public interface SimpleBeanHome extends EJBLocalHome {
    SimpleBean create() throws javax.ejb.CreateException;
}