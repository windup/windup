package com.jboss.windup.regression.ejb3;

import javax.ejb.Stateless;

/**
 * Session Bean implementation class TestSessionBean2
 */
@Stateless
public class TestSessionBean implements TestSessionBeanRemote, TestSessionBeanLocal {

    /**
     * Default constructor. 
     */
    public TestSessionBean() {
        
    }

}
