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

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.graph.clz.ProfileClz;

/**
 * @author Navin Surtani
 */
public class ProfileArchive implements Profile
{
   /** Archive name */
   private String archiveName;

   /** The module identifier */
   private String moduleIdentifier;

   /** Set of classes*/
   private Set<ProfileClz> classes;

   /**
    * Constructor
    *
    * @param archiveName         - the name of the archive.
    * @param moduleIdentifier    - the module identifier String.
    */
   public ProfileArchive(String archiveName, String moduleIdentifier)
   {
      this.archiveName = archiveName;
      this.moduleIdentifier = moduleIdentifier;
      this.classes = new HashSet<ProfileClz>();
   }

   /**
    * Whether or not the class passed is provided in the local set of class names.
    *
    * @param clz  - the string representation of the class.
    * @return     - whether or not the class name exists in the local set.
    */
   public boolean doesProvide(String clz)
   {
      return classes.contains(clz);
   }
   
   @Override
   public Set<ProfileClz> getProvided() {
	   return classes;
   }

   /**
    * Gets the name of the archive.
    *
    * @return  - the name of the archive.
    */
   public String getName()
   {
      return archiveName;
   }

   /**
    * Gets the name of the module identifier.
    *
    * @return  - the module identifier.
    */
   public String getModuleIdentifier()
   {
      return moduleIdentifier;
   }

   /**
    * Adds the parameter to the local set of classes.
    *
    * @param className  - the name of the class to add.
    */
   public void addClass(ProfileClz className)
   {
      classes.add(className);
   }
}
