/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.config.parser;

import org.jboss.windup.config.parser.ElementHandler;

/**
 * Identifies an {@link ElementHandler} by its tag name and namespace.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class HandlerId
{
   final private String namespace;
   final private String tagName;

   public HandlerId(String namespace, String tagName)
   {
      this.tagName = tagName;
      this.namespace = namespace;
   }

   public String getTagName()
   {
      return tagName;
   }

   public String getNamespace()
   {
      return namespace;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
      result = prime * result + ((tagName == null) ? 0 : tagName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      HandlerId other = (HandlerId) obj;
      if (namespace == null)
      {
         if (other.namespace != null)
            return false;
      }
      else if (!namespace.equals(other.namespace))
         return false;
      if (tagName == null)
      {
         if (other.tagName != null)
            return false;
      }
      else if (!tagName.equals(other.tagName))
         return false;
      return true;
   }

}
