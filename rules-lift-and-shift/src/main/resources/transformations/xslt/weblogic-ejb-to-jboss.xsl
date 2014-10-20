<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"
        doctype-system="http://www.jboss.org/j2ee/dtd/jboss_5_0.dtd" doctype-public="-//JBoss//DTD JBOSS 5.0//EN"/>

    <!-- Starting point -->
    <xsl:template match="/weblogic-ejb-jar">
        <jboss>
            <enterprise-beans>
                <xsl:apply-templates select="weblogic-enterprise-bean"/>
            </enterprise-beans>
        </jboss>
    </xsl:template>

    <!-- Template for Weblogic Stateless Session Beans -->
    <xsl:template match="weblogic-enterprise-bean[.//stateless-session-descriptor]">
        <session>
            <ejb-name>
                <xsl:value-of select="ejb-name"/>
            </ejb-name>
            <jndi-name>
                <xsl:value-of select="jndi-name"/>
            </jndi-name>
            <local-jndi-name>
                <xsl:text>java:</xsl:text>
                <xsl:value-of select="jndi-name"/>
            </local-jndi-name>
            <xsl:if
                test="stateless-session-descriptor/stateless-clustering/stateless-bean-is-clusterable = 'True' or stateless-session-descriptor/stateless-clustering/stateless-bean-is-clusterable = 'true'">
                <clustered>
                    <xsl:text>true</xsl:text>
                </clustered>
            </xsl:if>
            <xsl:if test="enable-call-by-reference = 'false' or enable-call-by-reference = 'False'">
                <call-by-value>
                    <xsl:text>true</xsl:text>
                </call-by-value>
            </xsl:if>
            <xsl:apply-templates select="reference-descriptor"/>
        </session>

    </xsl:template>

    <!-- Template for Weblogic Entity Beans -->
    <xsl:template match="weblogic-enterprise-bean[.//entity-descriptor]">
        <entity>
            <ejb-name>
                <xsl:value-of select="ejb-name"/>
            </ejb-name>
            <jndi-name>
                <xsl:value-of select="jndi-name"/>
            </jndi-name>
            <local-jndi-name>
                <xsl:text>java:</xsl:text>
                <xsl:value-of select="jndi-name"/>
            </local-jndi-name>
            <read-only>true</read-only>
            <configuration-name>FIXME: Entity Bean Configuration Name</configuration-name>
            <xsl:if
                test="entity-descriptor/entity-clustering/home-is-clusterable = 'True' or entity-descriptor/entity-clustering/home-is-clusterable = 'true'">
                <clustered>
                    <xsl:text>true</xsl:text>
                </clustered>
            </xsl:if>
            <xsl:if test="enable-call-by-reference = 'false' or enable-call-by-reference = 'False'">
                <call-by-value>
                    <xsl:text>true</xsl:text>
                </call-by-value>
            </xsl:if>
        </entity>
    </xsl:template>

    <!-- Template for Weblogic Message Driven Beans -->
    <xsl:template match="weblogic-enterprise-bean[.//message-driven-descriptor]">
        <message-driven>
            <ejb-name>
                <xsl:value-of select="ejb-name"/>
            </ejb-name>
            <destination-jndi-name>
                <xsl:text>/topic/</xsl:text>
                <xsl:value-of select="message-driven-descriptor/destination-jndi-name"/>
            </destination-jndi-name>
            <xsl:apply-templates select="reference-descriptor"/>
        </message-driven>
    </xsl:template>

    <xsl:template match="reference-descriptor">
        <xsl:apply-templates select="ejb-reference-description"/>
    </xsl:template>

    <xsl:template match="ejb-reference-description">
        <xsl:variable name="refName" select="jndi-name"/>
        <ejb-ref>
            <ejb-ref-name>
                <xsl:text>ejb/</xsl:text>
                <xsl:value-of select="$refName"/>
            </ejb-ref-name>
            <jndi-name>
                <xsl:value-of select="$refName"/>
            </jndi-name>
        </ejb-ref>
    </xsl:template>
</xsl:stylesheet>
