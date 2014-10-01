package com.tinkerpop.frames.util;


public class ExceptionUtils {

	public static void sneakyThrow(final Throwable checkedException) {
		ExceptionUtils.<RuntimeException> thrownInsteadOf(checkedException);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void thrownInsteadOf(Throwable t) throws T {
		throw (T) t;
	}
}
