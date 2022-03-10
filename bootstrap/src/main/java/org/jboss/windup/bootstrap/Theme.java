package org.jboss.windup.bootstrap;

// We need duplicate this file and have it here as a Workaround for not being able
// to consume org.jboss.windup.util.* because of java.lang.NoClassDefFoundError in windup-distribution
// TODO: Fix this issue in https://issues.redhat.com/browse/WINDUP-3283 and then remove this block of code
public class Theme {

    private final String brandNameLong;
    private final String brandNameAcronym;
    private final String brandDocumentationUrl;
    private final String cliName;

    public Theme(String brandNameLong, String brandNameAcronym, String brandDocumentationUrl, String cliName) {
        this.brandNameLong = brandNameLong;
        this.brandNameAcronym = brandNameAcronym;
        this.brandDocumentationUrl = brandDocumentationUrl;
        this.cliName = cliName;
    }

    public String getBrandNameLong() {
        return brandNameLong;
    }

    public String getBrandNameAcronym() {
        return brandNameAcronym;
    }

    public String getBrandDocumentationUrl() {
        return brandDocumentationUrl;
    }

    public String getCliName() {
        return cliName;
    }

}
