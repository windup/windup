<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:rl="http://www.ibm.com/xmlns/prod/websphere/wbi/br/6.0.0">

    <xsl:output omit-xml-declaration="yes" method="text" encoding="UTF-16"/>

    <xsl:template match="/rl:RuleSet">
        <xsl:apply-templates select="rl:template"/>
        <xsl:apply-templates select="rl:RuleBlock"/>
    </xsl:template>


    <xsl:template match="rl:template">
        <xsl:text>

template "</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>"
</xsl:text>
        <xsl:variable name="templateId" select="rl:id"/>
        <xsl:apply-templates select="rl:rule"/>


        <xsl:text>
end template

</xsl:text>
        <xsl:text>
========== TEMPLATE DATA ===============</xsl:text>
        <xsl:for-each select="//rl:RuleBlock/rl:rule/rl:templateRef">
            <xsl:if test=". = $templateId">
                <xsl:apply-templates select="."/>
                <xsl:text>
				
				</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>
=======================================</xsl:text>
    </xsl:template>

    <xsl:template match="rl:rule">
        <xsl:text>rule "</xsl:text>
        <xsl:value-of select="../@name"/>
        <xsl:text> @{row.rowNumber}"</xsl:text>
        <xsl:if test="../../rl:rulegroup">
            <xsl:text>
	ruleflow-group "</xsl:text>
            <xsl:value-of select="../../rl:rulegroup"/>
            <xsl:text>"</xsl:text>
        </xsl:if>
        <xsl:apply-templates select="rl:if"/>
        <xsl:apply-templates select="rl:then"/>

        <xsl:text>
end</xsl:text>
    </xsl:template>


    <xsl:template match="rl:if">
        <xsl:text>
when
</xsl:text>
        <xsl:apply-templates select="rl:conditionExpression"/>

    </xsl:template>


    <xsl:template match="rl:conditionExpression[@xsi:type='rl:LogicalOrExpression']">
        <xsl:text>(</xsl:text>
        <xsl:for-each select="rl:conditionExpression">
            <xsl:apply-templates select="."/>
            <xsl:if test="position() != last()">
                <xsl:text> || </xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:text>)</xsl:text>
    </xsl:template>


    <xsl:template match="rl:conditionExpression[@xsi:type='rl:LogicalAndExpression']">
        <xsl:text>(</xsl:text>
        <xsl:for-each select="rl:conditionExpression">
            <xsl:apply-templates select="."/>
        </xsl:for-each>
        <xsl:text>)</xsl:text>
    </xsl:template>

    <xsl:template match="rl:conditionExpression[@xsi:type='rl:BooleanExpression']">
        <xsl:apply-templates select="rl:expString"/>
    </xsl:template>



    <xsl:template match="rl:expString">
        <xsl:text></xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>
</xsl:text>
    </xsl:template>

    <xsl:template match="rl:then">
        <xsl:text>
then
</xsl:text>
        <xsl:apply-templates select="rl:Action"/>
    </xsl:template>

    <xsl:template match="rl:Action">
        <xsl:text>	</xsl:text>
        <xsl:value-of select="@value"/>
        <xsl:text>;
</xsl:text>
    </xsl:template>

    <xsl:template match="rl:RuleBlock">
        <xsl:apply-templates select="rl:rule[@type='rl:TemplateInstanceRule']"/>
    </xsl:template>

    <xsl:template match="rl:templateRef">
        <xsl:apply-templates select="../rl:parameterValue"/>
    </xsl:template>

    <xsl:template match="rl:parameterValue">
        <xsl:text>
		</xsl:text>
        <xsl:value-of select="rl:name"/>
        <xsl:text> = "</xsl:text>
        <xsl:value-of select="rl:value/@value"/>
        <xsl:text>"</xsl:text>
    </xsl:template>


</xsl:stylesheet>