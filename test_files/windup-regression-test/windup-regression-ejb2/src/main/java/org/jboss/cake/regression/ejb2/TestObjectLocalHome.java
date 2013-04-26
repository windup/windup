package com.jboss.windup.regression.ejb2;

import javax.ejb.*;
import java.rmi.*;

public interface TestObjectLocalHome extends EJBLocalHome {
  public TestObject create() throws RemoteException, CreateException;
}