package org.jboss.windup.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class NamespaceUtils {

	public static String extractVersion(String schemaLocation) {
		if(StringUtils.isBlank(schemaLocation)) {
			return null;
		}
		
		Pattern pattern = Pattern.compile("[0-9][0-9a-zA-Z_]+.xsd$");
		Matcher matcher = pattern.matcher(schemaLocation);
		if(matcher.find()) {
			String match = matcher.group();
			
			//for system ID, make sure to remove the ".dtd" that could come in.
			String version = StringUtils.removeEnd(match, ".xsd");
			version = StringUtils.replace(version, "_", ".");
			version = StringUtils.trimToNull(version);
			return version;
		}
		
		return null;
	}
}
