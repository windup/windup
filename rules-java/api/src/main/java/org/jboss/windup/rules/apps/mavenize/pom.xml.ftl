<?xml version="1.0" encoding="UTF-8"?>
<#--
This file should be kept aligned with JBoss EAP Quickstarts.
Template input:
pom: class Pom
-->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <#-- These constructs are intended for correct formatting of the resulting pom.xml. -->
    <#assign i1 = ""?left_pad(1*4)>
    <#assign i2 = ""?left_pad(2*4)>
    <#assign i3 = ""?left_pad(3*4)>
    <#assign i4 = ""?left_pad(4*4)>

    <#if pom.parent??>
    <parent>
        <groupId>${pom.parent.coords.groupId}</groupId>
        <artifactId>${pom.parent.coords.artifactId}</artifactId>
        <version>${pom.parent.coords.version}</version>
    </parent>
    </#if>

    <groupId>${pom.coords.groupId}</groupId>
    <artifactId>${pom.coords.artifactId}</artifactId>
    <#if pom.coords.version??>${i1}<version>${pom.coords.version}</version>${"\n"}</#if><#t><#-- Null if same as parent. -->
    <#if pom.coords.packaging??>${i1}<packaging>${pom.coords.packaging}</packaging>${"\n"}</#if><#t>
    <#if pom.coords.classifier??>${i1}<classifier>${pom.coords.classifier}</classifier>${"\n"}</#if><#t>

    <#if pom.name??>${i1}<name>${pom.name}</name>${"\n"}</#if><#t>
    <#if pom.description??>${i1}<description>${pom.description}</description>${"\n"}</#if><#t>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <!-- maven-compiler-plugin -->
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.source>1.8</maven.compiler.source>

        <version.plugin.surefire>2.10</version.plugin.surefire>
        <version.plugin.war>2.1.1</version.plugin.war>
        <version.plugin.exec>1.2.1</version.plugin.exec>
    </properties>

    <#if (pom.coords.packaging!"pom") = "pom" && pom.submodules?has_content>
    <modules>
        <#list pom.submodules?keys as modulePath>
        <module>${modulePath}</module>
        </#list>
    </modules>

    </#if>
    <#if pom.bom??>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>${pom.bom.groupId}</groupId>
                <artifactId>${pom.bom.artifactId}</artifactId>
                <version>${pom.bom.version}</version>
                <#if pom.bom.classifier??>${i4}<classifier>${pom.bom.classifier}</classifier>${"\n"}</#if><#t>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    </#if>
    <#list pom.dependencies>
    <dependencies>
        <#items as dep>
        <dependency>
            <groupId>${dep.groupId}</groupId>
            <artifactId>${dep.artifactId}</artifactId>
            <#if dep.version??>${i3}<version>${dep.version}</version>${"\n"}</#if><#t>
            <#if dep.classifier!?trim?has_content>${i3}<classifier>${dep.classifier}</classifier>${"\n"}</#if><#t>
            <#if (dep.packaging!"jar") != "jar">${i3}<type>${dep.packaging!"jar"}</type>${"\n"}</#if><#t>
            <#if (dep.scope!"compile") != "compile">${i3}<scope>${dep.scope}</scope>${"\n"}</#if><#t>
        </dependency>
        </#items>
    </dependencies>
    </#list>

    <#if pom.isRoot()>
    <!-- Activate JBoss Product Maven repository.

        NOTE: Configuring the Maven repository in the pom.xml file is not a recommended procedure
        and is only done here to make it easier.
        See the section entitled 'Use the Maven Repository' in the Development Guide for Red Hat JBoss EAP:
        https://access.redhat.com/documentation/en/jboss-enterprise-application-platform/
    -->
    <repositories>
        <repository>
            <id>jboss-enterprise-maven-repository</id>
            <url>https://maven.repository.redhat.com/ga/</url>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>jboss-enterprise-maven-repository</id>
            <url>https://maven.repository.redhat.com/ga/</url>
        </pluginRepository>
    </pluginRepositories>

    <build>
        <plugins>
            <!-- The WildFly plug-in deploys your EAR to a local JBoss EAP container. -->
            <plugin>
                <groupId>org.wildfly.plugins</groupId>
                <artifactId>wildfly-maven-plugin</artifactId>
                <version>1.0.2.Final</version>
                <configuration>
                    <!-- Due to Maven's lack of intelligence with EARs we need to skip deployment for all modules.
                         We then enable it specifically in the EAR module. -->
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>

    </#if>
    <#if pom.coords.packaging = "ear">
    <build>
        <finalName>${"$"}{project.parent.artifactId}</finalName>
        <plugins>
            <!-- EAR plug-in -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-ear-plugin</artifactId>
                <version>2.10.1</version>
                <!-- configuring the EAR plug-in -->
                <configuration>
                    <!-- Tell Maven to generate Java EE 7 application.xml. -->
                    <version>7</version>
                    <!-- Use Java EE EAR libraries as needed. Java EE ear libraries
                        are in easy way to package any libraries needed in the EAR, and automatically
                        have any modules (EJB-JARs and WARs) use them. -->
                    <defaultLibBundleDir>lib</defaultLibBundleDir>
                    <modules>
                    <!-- Default context root of the web app is /jboss-ejb-in-ear-web.
                        If a custom context root is needed, uncomment the following snippet to
                        register our War as a web module and set the contextRoot property.
                        <webModule>
                            <groupId>${"$"}{project.groupId}</groupId>
                            <artifactId>...-web</artifactId>
                            <contextRoot>/${"$"}{project.parent.artifactId}-ear</contextRoot>
                        </webModule>
                    -->
                    </modules>
                    <fileNameMapping>no-version</fileNameMapping>
                </configuration>
            </plugin>

            <!-- WildFly plug-in to deploy EAR -->
            <plugin>
                <groupId>org.wildfly.plugins</groupId>
                <artifactId>wildfly-maven-plugin</artifactId>
                <configuration>
                    <skip>false</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>

    </#if>
    <#if pom.coords.packaging = "war">
    <build>
        <finalName>${"$"}{project.artifactId}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>${"$"}{war.plugin.version}</version>
                <configuration>
                    <!-- Java EE doesn't require web.xml. -->
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                    <archive>
                        <addMavenDescriptor>false</addMavenDescriptor>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
    </#if>
</project>
