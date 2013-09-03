<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
	xmlns:wl="http://www.bea.com/ns/weblogic/90"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="urn:hornetq"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="urn:hornetq ../schemas/hornetq-jms.xsd"
	exclude-result-prefixes="h"
>
	<xsl:output method="xml" omit-xml-declaration="yes"/>
	

	<!-- Starting point -->
	<xsl:template match="/wl:weblogic-jms">
		<xsl:element name="configuration">
			<xsl:apply-templates select="//wl:queue"/>
			<xsl:apply-templates select="//wl:topic"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="wl:queue">
		<xsl:variable name="name" select="./@name"/>
		<xsl:variable name="jndi" select="./wl:jndi-name/text()"/>
		<xsl:element name="queue">
			<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
			<xsl:element name="entry">
				<xsl:attribute name="name"><xsl:value-of select="$jndi"/></xsl:attribute>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	
	
	<xsl:template match="wl:topic">
		<xsl:variable name="name" select="./@name"/>
		<xsl:variable name="jndi" select="./wl:jndi-name/text()"/>
		<xsl:element name="topic">
			<xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
			<xsl:element name="entry"><xsl:value-of select="$jndi"/></xsl:element>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
