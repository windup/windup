package com.jboss.windup.regression.ejb2;

import javax.ejb.*;
import java.rmi.*;
import java.util.*;

public interface TestObject extends EJBObject {
  public void addBook(String bname) throws RemoteException;

  //public Vector getBooks() throws RemoteException;
}
