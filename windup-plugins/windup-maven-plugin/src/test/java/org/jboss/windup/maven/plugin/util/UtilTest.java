package org.jboss.windup.maven.plugin.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilTest {
	
	@Test
	public void arrayToStringTest() {
		String[] testArray = new String[]{"org.jboss.windup","org.apache"};
		
		String testArrayResult = WindupUtils.convertArrayToString(testArray, ':');
		
		assertEquals("org.jboss.windup:org.apache", testArrayResult);
	}

}
