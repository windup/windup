<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exsl="http://exslt.org/common" version="1.0">

	<xsl:param name="v1" select="'http://java.sun.com/xml/ns/persistence'" />
	<xsl:param name="v2" select="'http://www.w3.org/2001/XMLSchema-instance'" />

	<xsl:template match="@* | node()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="persistence">
	<xsl:text>&#xa;</xsl:text>
		<xsl:element name="persistence" namespace="{$v1}">
			<xsl:attribute name="xsi:schemaLocation" namespace="{$v2}">http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd</xsl:attribute>
			<xsl:attribute name="version">1.0</xsl:attribute>
			<xsl:apply-templates select="@* | node()" />
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>			
