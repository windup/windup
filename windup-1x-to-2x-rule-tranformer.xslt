<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® -->
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:util="http://www.springframework.org/schema/util" xmlns:windup="http://www.jboss.org/schema/windup"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
                        http://www.springframework.org/schema/util
                        http://www.springframework.org/schema/util/spring-util-2.5.xsd
                        http://www.jboss.org/schema/windup
                        http://www.jboss.org/schema/windup.xsd">

<xsl:template match="windup:pipeline">
    <xsl:apply-templates select="./*"/>
</xsl:template>

<xsl:template match="windup:file-gate">
<xsl:variable name="file" select="@regex" /> 
<xsl:for-each select="windup:decorators/windup:xpath-value">
<xsl:call-template name="xpath-value">
         <xsl:with-param name="file" select="$file"/>
</xsl:call-template>
</xsl:for-each>

<xsl:for-each select="windup:decorators/windup:xpath-classification">
<xsl:call-template name="xpath-classification">
         <xsl:with-param name="file" select="$file"/>
</xsl:call-template>
</xsl:for-each>


 </xsl:template>

<xsl:template name="xmlFile"> 
<xsl:param name="file"/>
<xsl:param name="nested"/>
<xsl:param name="publicId"/>
.addRule() <br/>
.when(
XmlFile

<xsl:choose>
  <xsl:when test="@xpath">
   .matchesXpath(<xsl:text>"</xsl:text><xsl:value-of select="@xpath" /> <xsl:text>"</xsl:text>)
  </xsl:when>
  <xsl:otherwise>
  </xsl:otherwise>
</xsl:choose> 


<xsl:for-each select="windup:namespace">
.namespace(
<xsl:text>"</xsl:text><xsl:value-of select="@prefix"/> <xsl:text>"</xsl:text>,
<xsl:text>"</xsl:text><xsl:value-of select="@uri"/> <xsl:text>"</xsl:text>
)
</xsl:for-each>

<xsl:choose>
  <xsl:when test="$file">
    .inFile(<xsl:text>"</xsl:text><xsl:value-of select="$file"/> <xsl:text>"</xsl:text>)
  </xsl:when>
  <xsl:otherwise>
  </xsl:otherwise>
</xsl:choose> 


<xsl:choose>
  <xsl:when test="$publicId">
    .withDTDPublicId(<xsl:text>"</xsl:text><xsl:value-of select="$publicId"/> <xsl:text>"</xsl:text>)
  </xsl:when>
  <xsl:otherwise>
  </xsl:otherwise>
</xsl:choose> 

<xsl:if test="windup:hints">
<xsl:if test="$nested != 'true'">
<xsl:text>.as("1").and(</xsl:text>
XmlFile.from("1").resultMatches(
<xsl:text>"</xsl:text><xsl:value-of select="windup:hints/windup:regex-hint/@regex" /> <xsl:text>"</xsl:text>
).as("2")
)
</xsl:if> 
</xsl:if> 
             )
<br/>
</xsl:template>

<xsl:template name="xpath-value"> 
<xsl:param name="file"/>
<xsl:call-template name="xmlFile">
         <xsl:with-param name="file" select="$file"/>
</xsl:call-template>

.perform(Hint.

<xsl:if test="windup:hints">
in("1").
</xsl:if> 
withText(<xsl:text>"</xsl:text><xsl:value-of select="@description" /> <xsl:text>"</xsl:text>)
<xsl:call-template name="effort"/>


)
<br/>
 </xsl:template>

<xsl:template name="effort"> 

<xsl:choose>
  <xsl:when test="@effort">
    .withEffort(<xsl:value-of select="@effort"/>)
  </xsl:when>
  <xsl:otherwise>
  </xsl:otherwise>
</xsl:choose> 

<xsl:apply-templates select="./windup:hints"/>

 </xsl:template>

<xsl:template name="xpath-classification"> 
<xsl:param name="file"/>
<xsl:call-template name="xmlFile">
         <xsl:with-param name="file" select="$file"/>
</xsl:call-template>

.perform(Classification.as(<xsl:text>"</xsl:text><xsl:value-of select="@description" /> <xsl:text>"</xsl:text>)
<xsl:call-template name="effort"/>
)
<br/>
 </xsl:template>

<xsl:template name="dtd-classification"> 
<xsl:param name="file"/>
<xsl:call-template name="xmlFile">
         <xsl:with-param name="file" select="$file"/>
         <xsl:with-param name="publicId" select="@public-id-regex"/>
</xsl:call-template>

.perform(Classification.as(<xsl:text>"</xsl:text><xsl:value-of select="@description" /> <xsl:text>"</xsl:text>)
<xsl:call-template name="effort"/>
)
<br/>
 </xsl:template>


<xsl:template match="windup:xpath-value">
<xsl:call-template name="xpath-value">
</xsl:call-template>
 </xsl:template>


<xsl:template match="windup:xpath-classification">
<xsl:call-template name="xpath-classification">
</xsl:call-template>
 </xsl:template>

<xsl:template match="windup:dtd-classification">
<xsl:call-template name="dtd-classification">
</xsl:call-template>
 </xsl:template>

<xsl:template match="windup:xpath-summary">
<xsl:call-template name="xpath-classification">
</xsl:call-template>
 </xsl:template>


<xsl:template match="windup:regex-hint">
.and(Hint.in("2").withText(<xsl:text>"</xsl:text><xsl:value-of select="@hint" /> <xsl:text>"</xsl:text>)
<xsl:if test="@effort">
.withEffort(<xsl:value-of select="@effort" /> )
</xsl:if> 
)
 </xsl:template>


</xsl:stylesheet>