package com.tinkerpop.frames;

import junit.framework.TestCase;

public class ClassUtilitiesTest extends TestCase {
    public void testGetGenericClassForParametrizedType() throws SecurityException, NoSuchMethodException {
    	assertEquals(Clazz.class, ClassUtilities.getGenericClass(ClassUtilitiesTest.class.getMethod("getSomething", Class.class)));
    }
    
    public static class Clazz {}
    
    public <T extends Clazz> Iterable<T> getSomething(Class<T> clazz) {
    	return null;
    }
}