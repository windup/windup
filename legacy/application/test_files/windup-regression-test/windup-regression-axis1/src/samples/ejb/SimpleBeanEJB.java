package samples.ejb;

import javax.ejb.*;

public class SimpleBeanEJB implements SessionBean {
    
    public void ejbCreate() {
    }

    public void ejbActivate() {}
    public void ejbPassivate()  {}
    public void ejbRemove() {}
    public void setSessionContext(SessionContext sc) {}

    // "Business" Methods:
    public String sayHello(String name) {
        return ( "Hello " + name + ", have a nice day.");
    }

    public String sayGoodbye(String name) {
        return ( "See ya, then " + name);
    }

}
