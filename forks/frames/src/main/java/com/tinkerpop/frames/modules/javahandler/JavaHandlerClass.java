package com.tinkerpop.frames.modules.javahandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * When using the {@link JavaHandler} you can change the implementation class
 * from the default to another class by using this annotation on your frame
 * class. For example:
 * </p>
 * 
 * <pre>
 * 
 * &#064;JavaHandlerClass(PersonImpl.class)
 * interface Person {
 * 
 *   &#064;JavaHandler
 *   public String doSomething(); 
 * 
 *   
 * }
 * 
 * abstract class PersonImpl implements Person, JavaHandlerContext<Vertex> {
 *   public String doSomething() {
 *     return "Use Frames!";
 *   }
 * }
 * 
 * </pre>
 * 
 * @author Bryn Cooke
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JavaHandlerClass {
	Class<?> value();
}
