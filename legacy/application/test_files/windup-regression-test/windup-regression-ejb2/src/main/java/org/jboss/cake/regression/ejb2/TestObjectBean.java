package com.jboss.windup.regression.ejb2;

import javax.ejb.*;
import java.util.*;

public class TestObjectBean implements SessionBean {

	private static final long serialVersionUID = -2644918257687259956L;
	String attr1;

  public TestObjectBean() {
	  attr1 = "";
  }

  public void setAttr1(String value) {
    attr1=value;
  }

  public String getAttr1() {
    return attr1;
  }

  public void ejbCreate() {
    System.out.println("calling ejbcreate....");
  }

  public void ejbRemove() {
    System.out.println("calling ejbremove");
  }

  public void ejbActivate() {
    System.out.println("calling ejbactivate");
  }

  public void ejbPassivate() {
    System.out.println("calling ejbpassivate");
  }

  public void setSessionContext(SessionContext sc) {
    System.out.println("calling session context");
  }

}