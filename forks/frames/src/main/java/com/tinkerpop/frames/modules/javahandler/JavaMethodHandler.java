package com.tinkerpop.frames.modules.javahandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.util.ExceptionUtils;

class JavaMethodHandler implements
		com.tinkerpop.frames.modules.MethodHandler<JavaHandler> {

	private JavaHandlerModule module;

	JavaMethodHandler(JavaHandlerModule module) {
		this.module = module;
	}

	@Override
	public Class<JavaHandler> getAnnotationType() {
		return JavaHandler.class;
	}



	@Override
	public Object processElement(Object framedElement, Method method,
			Object[] arguments, JavaHandler annotation,
			FramedGraph<?> framedGraph, Element element) {
		try {
			Object handler = module.createHandler(framedElement, framedGraph, element, method.getDeclaringClass(), method);
			return method.invoke(handler, arguments);
		} catch (IllegalArgumentException e) {
			throw new JavaHandlerException("Problem calling Java handler", e);
		} catch (IllegalAccessException e) {
			throw new JavaHandlerException("Problem calling Java handler", e);
		} catch (InvocationTargetException e) {
			ExceptionUtils.sneakyThrow(e.getTargetException());
			return null;
		}
	}

}
