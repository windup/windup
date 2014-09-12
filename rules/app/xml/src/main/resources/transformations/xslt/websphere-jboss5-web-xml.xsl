<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE jboss-web PUBLIC "-//JBoss//DTD Web Application 5.0//EN" "http://www.jboss.org/j2ee/dtd/jboss-web_5_0.dtd"&gt;
		</xsl:text>

        <jboss-web>
            <!-- http://localhost:8080/[context root]/[applicationURL] -->
            <context-root>
                <xsl:value-of select="//*[ local-name() = 'context-root']/@uri"/>
            </context-root>
        </jboss-web>

    </xsl:template>
</xsl:stylesheet>