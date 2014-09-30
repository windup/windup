package com.tinkerpop.frames.modules.javahandler;


/**
 * {@link JavaHandlerModule} uses this interface to create the concrete classes that will handle the method calls.
 * @author Bryn Cooke
 */
public interface JavaHandlerFactory {
	
	/**
	 * @param handlerClass The class to create.
	 * @return The created class.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> T create(Class<T> handlerClass) throws InstantiationException, IllegalAccessException;
}
