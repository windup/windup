<?xml version="1.0" encoding="UTF-8"?>
<#--
pom: class Pom
-->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <#-- These constructs are intended for correct formatting of the resulting pom.xml. -->
    <#assign i1 = ""?left_pad(1*4)>
    <#assign i2 = ""?left_pad(2*4)>
    <#assign i3 = ""?left_pad(3*4)>
    <#assign i4 = ""?left_pad(4*4)>

    <properties>
    </properties>

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

    <dependencyManagement>
        <dependencies>

            <!-- JBoss distributes a complete set of Java EE APIs including a Bill
                of Materials (BOM). A BOM specifies the versions of a "stack" (or a collection)
                of artifacts. We use this here so that we always get the correct versions
                of artifacts. Here we use the jboss-eap-javaee7 stack (you can
                read this as the JBoss stack of the Java EE APIs and related components.  -->
            <dependency>
                <groupId>org.jboss.bom</groupId>
                <#switch (options.targetPlatform)!>
                    <#case 6>
                <artifactId>jboss-javaee-6.0-with-all</artifactId>
                <version>1.0.7.Final</version>
                    <#break>
                    <#case 7>
                    <#default>
                <artifactId>wildfly-javaee7-with-tools</artifactId>
                <version>10.0.1.Final</version>
                </#switch>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

        <#list pom.dependencies as dep><#-- MavenCoords -->
            <dependency>
                <groupId>${dep.groupId}</groupId>
                <artifactId>${dep.artifactId}</artifactId>
                <version>${dep.version}</version>
                <#if dep.classifier??><classifier>${dep.classifier}</classifier></#if><#t>
                <#if (dep.packaging!"jar") != "jar"><type>${dep.packaging}</type></#if><#t>
            </dependency>

        </#list>
        </dependencies>
    </dependencyManagement>
</project>
