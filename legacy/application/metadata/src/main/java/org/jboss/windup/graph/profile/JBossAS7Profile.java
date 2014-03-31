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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.jboss.windup.graph.clz.ProfileClz;

import javassist.bytecode.ClassFile;

/**
 * Profile for JBoss AS 7.
 *
 * @author Navin Surtani
 */
public class JBossAS7Profile extends AbstractProfile implements ExtendedProfile
{
   private static final String CLASS_SET = "jbossas7.clz.gz";
   private static final String PROFILE_NAME = "JBoss AS 7";
   private static final String PROFILE_CODE = "as7";
   private static final String PROFILE_LOCATION = "jboss-modules.jar";
   private static final int CLASSFILE_VERSION = ClassFile.JAVA_6;

   /** Constructor */
   public JBossAS7Profile()
   {
      super(PROFILE_NAME, CLASSFILE_VERSION, PROFILE_LOCATION);
      this.loadProfile(CLASS_SET);
   }

   /**
    * Implementation from {@link ExtendedProfile}.
    *
    * @param clz  - the class name
    * @return     - the module identifier
    */

   public String getModuleIdentifier(String clz)
   {
      for (Profile p : subProfiles)
      {
         if (p.doesProvide(clz))
         {
            return p.getModuleIdentifier();
         }
      }
      return null;
   }

   /**
    * Makes the call on the superclass.
    *
    * @param clz The class name
    * @return - whether or not the class name is provided or not.
    */
   public boolean doesProvide(String clz)
   {
      return super.doesProvide(clz);
   }

   /**
    * The name of the Profile
    *
    * @return  - the name of the profile
    */

   public String getName()
   {
      return PROFILE_NAME;
   }

   @Override
   public String getProfileCode()
   {
      return PROFILE_CODE;
   }

   @Override
   public String getProfileName()
   {
      return PROFILE_NAME;
   }

   @Override
   protected void loadProfile(String classSet)
   {
      InputStream inputStream = null;
      try
      {
         inputStream = this.getClass().getClassLoader().getResourceAsStream("profiles/"+classSet);
         GZIPInputStream gis = new GZIPInputStream(inputStream);
         InputStreamReader isr = new InputStreamReader(gis);
         BufferedReader br = new BufferedReader(isr);
         Map <String, ProfileArchive> profileMapping = new HashMap<String, ProfileArchive>();

         String s = br.readLine();
         while (s != null)
         {
            StringTokenizer tokenizer = new StringTokenizer(s, ",");
            String className = tokenizer.nextToken();
            String archiveName = tokenizer.nextToken();
            String moduleIdentifier = "";

            if (tokenizer.hasMoreTokens())
               moduleIdentifier = tokenizer.nextToken();

            ProfileArchive profileArchive = profileMapping.get(archiveName);

            if (profileArchive == null)
            {
               profileArchive = new ProfileArchive(archiveName, moduleIdentifier);
               profileMapping.put(archiveName, profileArchive);
            }

            profileArchive.addClass(new ProfileClz(className, this));
            s = br.readLine();
         }
         subProfiles.addAll(profileMapping.values());
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
      }
      finally
      {
         try
         {
            if (inputStream != null)
            {
               inputStream.close();
            }
         }
         catch (IOException closeException)
         {
            // No op.
         }
      }
   }
}
