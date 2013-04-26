<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:exsl="http://exslt.org/common" version="1.0">
<xsl:param name="v1" select="'jboss.messaging.destination:service=Queue,name='" />
<xsl:param name="v2" select="'http://www.w3.org/2001/XMLSchema-instance'" />
<xsl:template match="@* | node()">
    <xsl:copy>
        <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
</xsl:template>
<xsl:template match="server">
    <xsl:element name="{local-name()}">
        <xsl:text>&#xa;</xsl:text>
        <xsl:apply-templates select="mbean" />
    </xsl:element>
</xsl:template>
<xsl:template match="mbean">
    <xsl:element name="{local-name()}">
        <xsl:choose>
            <xsl:when test="@code='org.jboss.jms.server.destination.QueueService' or @code='org.jboss.mq.server.jmx.Queue'">
                <xsl:attribute name="code">org.jboss.jms.server.destination.QueueService</xsl:attribute>
                <xsl:attribute name="name">
                    jboss.messaging.destination:service=Queue,name=
                    <xsl:value-of
                    select="substring-after(@name,'name=')" />
                </xsl:attribute>
                <xsl:text>&#xa;</xsl:text>
                <xsl:for-each select="attribute">
                    <xsl:if test="not(@name='RedeliveryLimit')">
                        <xsl:copy>
                            <xsl:apply-templates select="@* | node()" />
                        </xsl:copy>
                    </xsl:if>
                    <xsl:if test="@name='RedeliveryLimit'">
                        <xsl:element name="{local-name()}">
                            <xsl:attribute name="name">MaxDeliveryAttempts</xsl:attribute>
                            <xsl:value-of select="." />
                        </xsl:element>
                    </xsl:if>
                    <xsl:text>&#xa;</xsl:text>
                </xsl:for-each>
                <depends optional-attribute-name="ServerPeer">jboss.messaging:service=ServerPeer</depends>
                <depends>jboss.messaging:service=PostOffice</depends>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@* | node()" />
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:element>
    <xsl:text>&#xa;</xsl:text>
</xsl:template>
</xsl:stylesheet>