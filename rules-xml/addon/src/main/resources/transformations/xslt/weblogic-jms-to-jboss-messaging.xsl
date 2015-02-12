<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:wl="http://www.bea.com/ns/weblogic/90" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>

    <!-- Starting point -->
    <xsl:template match="/wl:weblogic-jms">
        <xsl:element name="server">
            <xsl:apply-templates select="//wl:queue"/>
            <xsl:apply-templates select="//wl:topic"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="wl:queue">
        <xsl:variable name="queueName" select="./@name"/>
        <xsl:element name="mbean">
            <xsl:attribute name="code">org.jboss.jms.server.destination.QueueService</xsl:attribute>
            <xsl:attribute name="name"><xsl:text>jboss.messaging.destination:service=Queue,name=</xsl:text><xsl:value-of
                select="$queueName"/></xsl:attribute>
            <xsl:attribute name="xbean-dd">xmdesc/Queue-xmbean.xml</xsl:attribute>

            <xsl:element name="depends">
                <xsl:attribute name="optional-attribute-name">ServerPeer</xsl:attribute>
                <xsl:text>jboss.messaging:service=ServerPeer</xsl:text>
            </xsl:element>

            <xsl:element name="depends">
                <xsl:text>jboss.messaging:service=PostOffice</xsl:text>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="wl:topic">
        <xsl:variable name="queueName" select="./@name"/>
        <xsl:element name="mbean">
            <xsl:attribute name="code">org.jboss.jms.server.destination.TopicService</xsl:attribute>
            <xsl:attribute name="name"><xsl:text>jboss.messaging.destination:service=Topic,name=</xsl:text><xsl:value-of
                select="$queueName"/></xsl:attribute>
            <xsl:attribute name="xbean-dd">xmdesc/Topic-xmbean.xml</xsl:attribute>

            <xsl:element name="depends">
                <xsl:attribute name="optional-attribute-name">ServerPeer</xsl:attribute>
                <xsl:text>jboss.messaging:service=ServerPeer</xsl:text>
            </xsl:element>

            <xsl:element name="depends">
                <xsl:text>jboss.messaging:service=PostOffice</xsl:text>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
