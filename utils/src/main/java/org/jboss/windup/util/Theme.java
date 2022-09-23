package org.jboss.windup.util;

public class Theme {

    private final String brandName;
    private final String brandNameAcronym;
    private final String brandWebsiteUrl;
    private final String brandDocumentationUrl;
    private final String cliName;
    private final String cliVersion;
    private final String componentsVersion;

    public Theme(String brandName, String brandNameAcronym, String brandWebsiteUrl, String brandDocumentationUrl, String cliName, String cliVersion, String componentsVersion) {
        this.brandName = brandName;
        this.brandNameAcronym = brandNameAcronym;
        this.brandWebsiteUrl = brandWebsiteUrl;
        this.brandDocumentationUrl = brandDocumentationUrl;
        this.cliName = cliName;
        this.cliVersion = cliVersion;
        this.componentsVersion = componentsVersion;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getBrandNameAcronym() {
        return brandNameAcronym;
    }

    public String getBrandWebsiteUrl() {
        return brandWebsiteUrl;
    }

    public String getBrandDocumentationUrl() {
        return brandDocumentationUrl;
    }

    public String getCliName() {
        return cliName;
    }

    public String getCliVersion() {
        return cliVersion;
    }

    public String getComponentsVersion() {
        return componentsVersion;
    }
}
