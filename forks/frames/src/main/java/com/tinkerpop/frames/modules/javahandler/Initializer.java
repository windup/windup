package com.tinkerpop.frames.modules.javahandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tinkerpop.frames.FrameInitializer;

/**
 * Allows methods in a java handler implementation to be called when a vertex or an edge is added to the graph.
 * This effectively has the same function as a {@link FrameInitializer}.
 * 
 * For each interface in the hierarchy initializer methods will be called.
 * 
 * <pre>
 * interface A {
 * 
 *   abstract class Impl implements A {
 *   
 *     &#064;Initializer
 *     void init() {
 *       //Called when a framed vertex or edge is added to the graph.
 *     }
 *   }
 * 
 * }
 * 
 * </pre>
 *  
 * @author Bryn Cooke
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Initializer {

	
	
}
