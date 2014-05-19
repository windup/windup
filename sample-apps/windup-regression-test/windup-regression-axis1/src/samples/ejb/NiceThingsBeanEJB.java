package samples.ejb;

import javax.ejb.*;

public class NiceThingsBeanEJB implements SessionBean {
    
    public void ejbCreate() {}

    public void ejbActivate() {}
    public void ejbPassivate()  {}
    public void ejbRemove() {}
    public void setSessionContext(SessionContext sc) {}

    // "Business" Methods:
    public String sayHello(String name) {
        return ( "Hiya " + name + ", how are you?");

    }

    public NiceThings findNiceThingsFor(String name) {
        // In reality our bean would probably be looking up these nice
        // things from an entity bean. In our case we'll just cheat :)
        
        NiceThings niceThings = new NiceThings("windup",
                                               23,
                                               "black as night");
        return niceThings;
    }
        
    public boolean updateNiceThingsFor(String name, NiceThings niceThings) {
        // In reality this bean would probably try and update nice things 
        // in the relevant entity bean(s) and return a boolean to indicate 
        // whether the update was successful or not. Again, we'll cheat.
        return true;
    }
        
}
