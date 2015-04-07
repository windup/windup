package com.tinkerpop.frames.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Helper methods for validating input parameters and state.
 */
public final class Validate {
	public static void assertArgument(boolean assertionResult, String message, Object... args) {
		if (!assertionResult)
			throw new IllegalArgumentException(format(message, args));
	}
	
	public static void assertNotNull(Object... args) {
		for (Object arg: args)
			if (arg == null)
				throw new NullPointerException();
	}
	
	public static String format(String message, Object... args) {
		ByteArrayOutputStream msgStream = new ByteArrayOutputStream();
		new PrintStream(msgStream, true).printf(message, args);
		return msgStream.toString();
	}
}