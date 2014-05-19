package samples.ejb;

import javax.ejb.EJBLocalObject;

public interface SimpleBean extends EJBLocalObject {
    public String sayHello(String name) ;
    public String sayGoodbye(String name) ;    
}