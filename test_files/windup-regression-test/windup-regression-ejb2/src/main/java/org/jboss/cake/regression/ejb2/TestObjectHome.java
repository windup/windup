package com.jboss.windup.regression.ejb2;

import javax.ejb.*;
import java.rmi.*;

public interface TestObjectHome extends EJBHome {
  public TestObject create() throws RemoteException, CreateException;
}