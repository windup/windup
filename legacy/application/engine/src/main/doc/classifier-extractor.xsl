<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sp="http://www.springframework.org/schema/beans">
	<xsl:output method="text"/>
	<xsl:template match="/">
		<xsl:apply-templates select="//sp:bean[@class='com.jboss.windup.decorator.xml.XPathClassifyingDecorator']/sp:property[@name='matchDescription']"/>
		<xsl:apply-templates select="//sp:bean[@class='com.jboss.windup.decorator.java.JavaClassifyingDecorator']/sp:property[@name='matchDescription']"/>
	</xsl:template>
	
	<xsl:template match="//sp:bean[@class='com.jboss.windup.decorator.xml.XPathClassifyingDecorator']/sp:property[@name='matchDescription']">
		<xsl:value-of select="@value"/>
		<xsl:text>
</xsl:text>
	</xsl:template>
	
	<xsl:template match="//sp:bean[@class='com.jboss.windup.decorator.java.JavaClassifyingDecorator']/sp:property[@name='matchDescription']">
		<xsl:value-of select="@value"/>
		<xsl:text>
</xsl:text>
	</xsl:template>
	
	
</xsl:stylesheet>