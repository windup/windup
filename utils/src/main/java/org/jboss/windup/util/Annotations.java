/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Stereotype;

/**
 * Utility class for common @{@link Annotation} operations.
 * <p/>
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com>Lincoln Baxter, III</a>
 */
public class Annotations
{
   /**
    * Discover if a Method <b>m</b> has been annotated with <b>type</b>.
    * 
    * @param m The method to inspect.
    * @param type The targeted annotation class
    * @return True if annotation is present either on the method itself, or on the declaring class of the method.
    *         Returns false if the annotation is not present.
    */
   public static boolean isAnnotationPresent(final Method m, final Class<? extends Annotation> type)
   {
      boolean result = false;
      if (m.isAnnotationPresent(type))
      {
         result = true;
      }
      else
      {
         for (Annotation a : m.getAnnotations())
         {
            if (isAnnotationPresent(a, type))
            {
               result = true;
               break;
            }
         }
      }

      if (!result)
      {
         result = isAnnotationPresent(m.getDeclaringClass(), type);
      }
      return result;
   }

   private static boolean isAnnotationPresent(final Annotation a, final Class<? extends Annotation> type)
   {
      boolean result = false;
      if (a.annotationType().isAnnotationPresent(type))
      {
         result = true;
      }
      else
      {
         for (Annotation nested : a.getClass().getAnnotations())
         {
            if (isAnnotationPresent(nested, type))
            {
               result = true;
               break;
            }
         }
      }

      return result;
   }

   /**
    * Discover if a Class <b>c</b> has been annotated with <b>type</b>. This also discovers annotations defined through
    * a @{@link Stereotype}.
    * 
    * @param c The class to inspect.
    * @param type The targeted annotation class
    * @return True if annotation is present either on class, false if the annotation is not present.
    */
   public static boolean isAnnotationPresent(final Class<?> c, final Class<? extends Annotation> type)
   {
      return getAnnotation(c, type) != null;
   }

   /**
    * Inspect method <b>m</b> for a specific <b>type</b> of annotation. This also discovers annotations defined through
    * a @ {@link Stereotype}.
    * 
    * @param m The method to inspect.
    * @param type The targeted annotation class
    * @return The annotation instance found on this method or enclosing class, or null if no matching annotation was
    *         found.
    */
   public static <A extends Annotation> A getAnnotation(final Method m, final Class<A> type)
   {
      A result = m.getAnnotation(type);
      if (result == null)
      {
         for (Annotation a : m.getAnnotations())
         {
            result = getAnnotation(a, type);

            if (result != null)
            {
               break;
            }
         }
      }
      if (result == null)
      {
         result = getAnnotation(m.getDeclaringClass(), type);
      }
      return result;
   }

   /**
    * Inspect annotation <b>a</b> for a specific <b>type</b> of annotation. This also discovers annotations defined
    * through a @ {@link Stereotype}.
    * 
    * @param m The method to inspect.
    * @param type The targeted annotation class
    * @return The annotation instance found on this method or enclosing class, or null if no matching annotation was
    *         found.
    */
   public static <A extends Annotation> A getAnnotation(final Annotation a, final Class<A> type)
   {
      Set<Annotation> seen = new HashSet<Annotation>();
      return getAnnotation(seen, a, type);
   }

   @SuppressWarnings("unchecked")
   private static <A extends Annotation> A getAnnotation(final Set<Annotation> seen, final Annotation a,
            final Class<A> type)
   {
      seen.add(a);

      A result = null;
      if (type.isAssignableFrom(a.getClass()))
      {
         result = (A) a;
      }

      if (result == null)
      {
         result = a.annotationType().getAnnotation(type);
      }

      if (result == null)
      {
         result = getAnnotation(a.annotationType().getDeclaringClass(), type);
      }

      if (result == null)
      {
         for (Annotation nested : a.annotationType().getAnnotations())
         {
            if (!seen.contains(nested))
            {
               result = getAnnotation(seen, nested, type);

               if (result != null)
               {
                  break;
               }
            }
         }
      }
      return result;
   }

   /**
    * Inspect class <b>c</b> for a specific <b>type</b> of annotation. This also discovers annotations defined through a @
    * {@link Stereotype}.
    * 
    * @param c The class to inspect.
    * @param type The targeted annotation class
    * @return The annotation instance found on this class, or null if no matching annotation was found.
    */
   public static <A extends Annotation> A getAnnotation(final Class<?> c, final Class<A> type)
   {
      A result = null;
      result = getAnnotationFromType(c, type);
      if (result == null)
      {
         Class<?> superclass = c.getSuperclass();
         while (superclass != null)
         {
            result = getAnnotation(superclass, type);
            if (result != null)
               break;
            superclass = superclass.getSuperclass();
         }
      }

      if (result == null)
      {
         Class<?>[] interfaces = c.getInterfaces();
         for (Class<?> i : interfaces)
         {
            result = getAnnotation(i, type);
            if (result != null)
               break;
         }
      }

      return result;
   }

   private static <A extends Annotation> A getAnnotationFromType(final Class<?> c, final Class<A> type)
   {
      if (c != null)
      {
         A result = c.getAnnotation(type);
         if (result == null)
         {
            for (Annotation a : c.getAnnotations())
            {
               if (a.annotationType().isAnnotationPresent(type))
               {
                  result = a.annotationType().getAnnotation(type);
               }
            }
         }
         return result;
      }
      return null;
   }
}
