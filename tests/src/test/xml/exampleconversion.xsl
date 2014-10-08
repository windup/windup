<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:web="http://java.sun.com/xml/ns/javaee">

	<xsl:template match="/">
		<output>
			<xsl:apply-templates select="//web:display-name"/>
		</output>
	</xsl:template>

	<xsl:template match="web:display-name">
		<display-name>
			The display name was: <xsl:value-of select="text()"/>
		</display-name>
	</xsl:template>


</xsl:stylesheet> 