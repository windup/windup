package samples.ejb;

import javax.ejb.EJBLocalHome;

public interface NiceThingsBeanHome extends EJBLocalHome {
    NiceThingsBean create() throws javax.ejb.CreateException;
}