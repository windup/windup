package org.jboss.windup.project.condition;

/**
 * Object used to specify the version range
 * @author mbriskar
 *
 */
public class Version{

	private String from;
	private String to;
	
	public static Version fromVersion(String from) {
		Version v = new Version();
		v.setFrom(from);
		return v;
	}
	
	public static Version toVersion(String to) {
		Version v = new Version();
		v.setTo(to);
		return v;
	}
	
	public Version to(String to) {
		this.setTo(to);
		return this;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	public boolean validate(String versionString) {
		boolean result = true;
		if(from != null) {
			result = result && firstVersionLesser(from,versionString);
		}
		if(to != null) {
			result =result && firstVersionLesser(versionString,to);
		}
		return result;
	}
	
	private boolean firstVersionLesser(String first, String second) {
		boolean firstLesser = false;
		for (int i=0;i<second.length();i++) {
			if(Character.isDigit(second.charAt(i))) {
				int numericValue = Character.getNumericValue(second.charAt(i));
				if(!firstLesser && first != null && Character.isDigit(first.charAt(i))) {
					 int firstInt = Character.getNumericValue(first.charAt(i));
					 if(firstInt < numericValue) {
						 firstLesser = true;
					 }
					 if(!firstLesser && (firstInt > numericValue)) {
						 return false;
					 }
				}
			}
		}
		return true;
	}
}
