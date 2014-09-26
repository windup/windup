//$Id: LoginTest.java 5810 2007-07-16 06:46:47Z gavin $
package org.jboss.seam.example.booking.test;

import org.jboss.seam.core.Manager;
import org.jboss.seam.web.Session;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class LoginTest extends SeamTest
{
   
   @Test
   public void testLoginComponent() throws Exception
   {
      new ComponentTest() {

         @Override
         protected void testComponents() throws Exception
         {
            assert getValue("#{identity.loggedIn}").equals(false);
            setValue("#{identity.username}", "gavin");
            setValue("#{identity.password}", "foobar");
            invokeMethod("#{identity.login}");
            assert getValue("#{user.name}").equals("Gavin King");
            assert getValue("#{user.username}").equals("gavin");
            assert getValue("#{user.password}").equals("foobar");
            assert getValue("#{identity.loggedIn}").equals(true);
            invokeMethod("#{identity.logout}");
            assert getValue("#{identity.loggedIn}").equals(false);
            setValue("#{identity.username}", "gavin");
            setValue("#{identity.password}", "tiger");
            invokeMethod("#{identity.login}");
            assert getValue("#{identity.loggedIn}").equals(false);
         }
         
      }.run();
   }
   
   @Test
   public void testLogin() throws Exception
   {
      
      new FacesRequest() {
         
         @Override
         protected void invokeApplication()
         {
            assert !isSessionInvalid();
            assert getValue("#{identity.loggedIn}").equals(false);
         }
         
      }.run();
      
      new FacesRequest() {

         @Override
         protected void updateModelValues() throws Exception
         {
            assert !isSessionInvalid();
            setValue("#{identity.username}", "gavin");
            setValue("#{identity.password}", "foobar");
         }

         @Override
         protected void invokeApplication()
         {
            invokeAction("#{identity.login}");
         }

         @Override
         protected void renderResponse()
         {
            assert getValue("#{user.name}").equals("Gavin King");
            assert getValue("#{user.username}").equals("gavin");
            assert getValue("#{user.password}").equals("foobar");
            assert !Manager.instance().isLongRunningConversation();
            assert getValue("#{identity.loggedIn}").equals(true);
         }
         
      }.run();
      
      new FacesRequest() {

         @Override
         protected void invokeApplication()
         {
            assert !isSessionInvalid();
            assert getValue("#{identity.loggedIn}").equals(true);
         }
         
      }.run();
      
      new FacesRequest() {

         @Override
         protected void invokeApplication()
         {
            assert !Manager.instance().isLongRunningConversation();
            assert !isSessionInvalid();
            invokeMethod("#{identity.logout}");
            assert Session.instance().isInvalid();
         }

         @Override
         protected void renderResponse()
         {
            assert getValue("#{identity.loggedIn}").equals(false);
            assert Session.instance().isInvalid();
         }
         
      }.run();
      
   }

}
