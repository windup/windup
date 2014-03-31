/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.windup.graph.profile;

import java.util.Set;

import org.jboss.windup.graph.clz.ProfileClz;
 


/**
 * Interface that will define what all Profiles need to implement.
 *
 * @author Navin Surtani
 */
public interface Profile
{
   /**
    * Method to check whether or not the String representation of the class parameter is provided or not.
    *
    * @param clz - the string representation of the class.
    * @return whether or not the class is provided.
    */
   public boolean doesProvide(String clz);

   
   public Set<ProfileClz> getProvided();

   /**
    * Simple getter.
    *
    * @return - the name of the profile.
    */
   public String getName();

   /**
    * Simple get call to obtain the module identifier for a given implementation.
    *
    * @return - the module identifier.
    */
   public String getModuleIdentifier();
}
