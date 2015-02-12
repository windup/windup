<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>

    <!-- Starting point -->
    <xsl:template match="/">
        <plugin>
            <groupId>org.apache.cxf</groupId>
            <artifactId>cxf-codegen-plugin</artifactId>
            <version>2.2.7</version>
            <executions>
                <execution>
                    <id>generate-sources</id>
                    <phase>generate-sources</phase>
                    <configuration>
                        <sourceRoot>${basedir}/src/main/java</sourceRoot>
                        <wsdlOptions>
                            <wsdlOption>
                                <wsdl>PATH_TO_WSDL</wsdl>
                            </wsdlOption>
                        </wsdlOptions>
                        <extraargs>
                            <extraarg>-keep</extraarg>
                            <extraarg>-impl</extraarg>
                            <extraarg>-verbose</extraarg>
                        </extraargs>
                    </configuration>
                    <goals>
                        <goal>wsdl2java</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </xsl:template>
</xsl:stylesheet>

