package org.jboss.windup.util;

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
