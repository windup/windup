package com.tinkerpop.frames;

import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.tinkerpop.frames.modules.MethodHandler;

/**
 * Thrown if a method could not be handled because an appropriate
 * {@link AnnotationHandler} or {@link MethodHandler} could not be found that
 * responds to the method
 * 
 * @author Bryn Cooke
 * 
 */
public class UnhandledMethodException extends RuntimeException {

	
	public UnhandledMethodException(String message) {
		super(message);
	}

}
