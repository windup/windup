package org.jboss.seam.example.booking;

import javax.ejb.Local;

@Local
public interface Authenticator
{
   boolean authenticate();
}
