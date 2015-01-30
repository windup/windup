package org.jboss.windup.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class DoctypeUtils {

	public static String extractVersion(String publicId, String systemId) {
		Pattern pattern = Pattern.compile("[0-9][0-9a-zA-Z.-]+");
		
		if(StringUtils.isNotBlank(publicId)) {
			Matcher matcher = pattern.matcher(publicId);
			if(matcher.find()) {
				return matcher.group();
			}
		}
		
		if(StringUtils.isNotBlank(systemId)) {
			Matcher matcher = pattern.matcher(systemId);
			if(matcher.find()) {
				String match = matcher.group();
				
				//for system ID, make sure to remove the ".dtd" that could come in.
				return StringUtils.removeEnd(match, ".dtd");
			}
		}
		
		return null;
	}
}
