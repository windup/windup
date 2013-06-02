package org.jboss.windup.maven.plugin.util;

public class WindupUtils {
	

	public static String convertArrayToString(String[] array, char seperator) {
		
		if(array == null) return null;
		
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i<array.length;i++) {
			sb.append(array[i]);
			
			if(i!=array.length-1) {
				sb.append(seperator);
			}
		}
		
		return sb.toString();
		
	}

}
