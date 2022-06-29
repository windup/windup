package org.jboss.windup.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Use a Java class to handle frame method calls. Unless overridden using the
 * {@link JavaHandlerClass} annotation the default handler will be a nested
 * class inside your frame interface called Impl. For example:
 * </p>
 * <pre>
 *
 * interface Person {
 *
 *   &#064;JavaHandler
 *   public String doSomething();
 *
 *   abstract class Impl implements Person, JavaHandlerContext {
 *     public String doSomething() {
 *       return "Use Frames!";
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * (Inspired directly from the Tinkerpop Frames implementation)
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JavaHandler {
    Class<?> handler();
}
