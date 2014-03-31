package samples.ejb;

import javax.ejb.EJBLocalObject;

public interface NiceThingsBean extends EJBLocalObject {
    public String sayHello(String name);
    
    public NiceThings findNiceThingsFor(String name);
    
    public boolean updateNiceThingsFor(String name, NiceThings niceThings);
}