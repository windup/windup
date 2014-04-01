<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no" />
	<xsl:template match="/">
	<xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE jboss-app PUBLIC "-//JBoss//DTD Java EE Application 5.0//EN" "http://www.jboss.org/j2ee/dtd/jboss-app_5_0.dtd"&gt;</xsl:text>
	<xsl:text>&#xa;</xsl:text>
	<xsl:copy-of select="jboss-app"/>

	</xsl:template>
</xsl:stylesheet>