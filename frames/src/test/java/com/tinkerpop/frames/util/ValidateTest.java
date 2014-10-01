package com.tinkerpop.frames.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ValidateTest {
	@Test public void assertNotNull() {
		Validate.assertNotNull();
		Validate.assertNotNull("x");
		Validate.assertNotNull("x", "y");
	}
	
	@Test(expected = NullPointerException.class) public void assertNotNullOneArg() {
		Validate.assertNotNull((Object)null);
	}
	
	@Test(expected = NullPointerException.class) public void assertMultipleArgs() {
		Validate.assertNotNull("x", null, "y");
	}
	
	@Test public void assertArgument() {
		assertArgumentThrowCheck(null, true, "message");
		assertArgumentThrowCheck(null, true, "message: %s", "x");
		assertArgumentThrowCheck("message", false, "message");
		assertArgumentThrowCheck("message: x", false, "message: %s", "x");
	}
	
	private static void assertArgumentThrowCheck(String throwMessage, boolean assertionResult, String message, Object... args) {
		boolean success = false;
		try {
			Validate.assertArgument(assertionResult, message, args);
			success = true;
		} catch (IllegalArgumentException e) {
			assertEquals(throwMessage, e.getMessage());
		}
		assertEquals(success, throwMessage == null);
	}
	
	
	@Test public void format() {
		assertEquals("aap - noot", Validate.format("%s - %s", "aap", "noot"));
	}

}
