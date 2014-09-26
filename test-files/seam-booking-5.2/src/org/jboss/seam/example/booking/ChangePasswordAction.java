//$Id: ChangePasswordAction.java 5579 2007-06-27 00:06:49Z gavin $
package org.jboss.seam.example.booking;

import static org.jboss.seam.ScopeType.EVENT;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;

@Stateful
@Scope(EVENT)
@Name("changePassword")
@Restrict("#{identity.loggedIn}")
public class ChangePasswordAction implements ChangePassword
{
   @In @Out
   private User user;
   
   @PersistenceContext
   private EntityManager em;
   
   private String verify;
   
   private boolean changed;
   
   @In
   private FacesMessages facesMessages;
   
   public void changePassword()
   {
      if ( user.getPassword().equals(verify) )
      {
         user = em.merge(user);
         facesMessages.add("Password updated");
         changed = true;
      }
      else 
      {
         facesMessages.addToControl("verify", "Re-enter new password");
         revertUser();
         verify=null;
      }
   }
   
   public boolean isChanged()
   {
      return changed;
   }
   
   private void revertUser()
   {
      user = em.find(User.class, user.getUsername());
   }
   public String getVerify()
   {
      return verify;
   }
   public void setVerify(String verify)
   {
      this.verify = verify;
   }
   
   @Remove
   public void destroy() {}
}
