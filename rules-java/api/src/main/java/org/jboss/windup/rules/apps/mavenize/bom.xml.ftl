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
        <groupId>${pom.parent.coord.groupId}</groupId>
        <artifactId>${pom.parent.coord.artifactId}</artifactId>
        <version>${pom.parent.coord.version}</version>
    </parent>
    </#if>

    <groupId>${pom.coord.groupId}</groupId>
    <artifactId>${pom.coord.artifactId}</artifactId>
    <#if pom.coord.version!?trim?has_content>${i1}<version>${pom.coord.version}</version>${"\n"}</#if><#t><#-- Null if same as parent. -->
    <#if pom.coord.packaging!?trim?has_content>${i1}<packaging>${pom.coord.packaging}</packaging>${"\n"}</#if><#t>
    <#if pom.coord.classifier!?trim?has_content>${i1}<classifier>${pom.coord.classifier}</classifier>${"\n"}</#if><#t>

    <#if pom.name!?trim?has_content>${i1}<name>${pom.name}</name>${"\n"}</#if><#t>
    <#if pom.description!?trim?has_content>${i1}<description>${pom.description}</description>${"\n"}</#if><#t>

    <dependencyManagement>
        <dependencies>

            <!-- JBoss distributes a complete set of Java EE APIs including a Bill
                of Materials (BOM). A BOM specifies the versions of a "stack" (or a collection)
                of artifacts. We use this here so that we always get the correct versions
                of artifacts. -->
            <dependency>
                <groupId>${pom.bom.groupId}</groupId>
                <artifactId>${pom.bom.artifactId}</artifactId>
                <version>${pom.bom.version}</version>
                <#if pom.bom.classifier!?trim?has_content>${i4}<classifier>${pom.bom.classifier}</classifier>${"\n"}</#if><#t>
                <type>pom</type>
                <scope>import</scope>
            </dependency>


        <#list pom.dependencies as dep><#-- MavenCoord -->
            <dependency>
                <groupId>${dep.coord.groupId}</groupId>
                <artifactId>${dep.coord.artifactId}</artifactId>
                <version>${dep.coord.version}</version>
                <#if dep.coord.classifier!?trim?has_content>${i4}<classifier>${dep.coord.classifier}</classifier>${"\n"}</#if><#t>
                <#if (dep.coord.packaging!"jar") != "jar">${i4}<type>${dep.coord.packaging}</type>${"\n"}</#if><#t>
            </dependency>

        </#list>
        </dependencies>
    </dependencyManagement>
</project>
