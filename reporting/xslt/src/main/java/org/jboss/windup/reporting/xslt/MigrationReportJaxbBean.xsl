<?xml version="1.0" encoding="UTF-8"?>
<!--
    Processed by Saxon HE 9.5.x.

    Author:  Ondrej Zizka, ozizka@redhat.com
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>

    <!-- Syntax at http://www.w3.org/TR/xslt -->
    <xsl:template match="/">
<html>
<head>
    <title>Migration report</title>
    <style type="text/css">
    </style>
    <link rel="stylesheet" type="text/css" href="MigrationReport.css"/>
    <script src="jQuery.js"/>
</head>
<body>
    <div class="header" style="background-color: #3B4D64; border-bottom: 1ex solid #243446;">
        <h1 style="padding: 1ex; color: white; margin: 0; ">Migration report</h1>
    </div>

    <div id="container">
        <h2><div class="icon"/><span>Summary</span></h2>
        <xsl:call-template name="MigrationSummary"/>

        <h2><div class="icon"/><span>Source server</span></h2>
        <h3>Files comparison against distribution archive</h3>
        <xsl:call-template name="ComparisonResult"/>

        <h2><div class="icon"/><span>Deployments</span></h2>
        <h3>Reports and Analyses - WindUp, TattleTale etc.</h3>
        <xsl:call-template name="Deployments"/>

        <h2><div class="icon"/><span>Source server configuration</span></h2>
        <xsl:call-template name="MigratorData"/>

        <h2><div class="icon"/><span>Actions to migrate to the target server</span></h2>
        <xsl:call-template name="Actions"/>

        <xsl:if test="finalException">
            <div class="finalException">
                <h2>Error</h2>
                <p><xsl:value-of select="finalException/text()"/></p>
            </div>
        </xsl:if>
    </div>
</body>
</html>
    </xsl:template>
    
    <!-- Summary -->
    <xsl:template name="MigrationSummary">
        <!--<xsl:value-of select="concat(name(), ' | ', position(), ' | ', count(child::*))"/>-->
        
        <table class="" style="margin: 2ex 0;">
            <tr>
                <th>Source server:</th>
                <td><xsl:value-of select="/migrationReport/sourceServer/@formatted"/></td>
                <td><xsl:value-of select="/migrationReport/sourceServer/@dir"/></td>
            </tr>
            <tr>
                <th>Target server:</th>
                <td>JBoss EAP 6.1</td>
            </tr>            
            <tr> <th>Dry run:</th>         <td><xsl:value-of select="/migrationReport/config/globalConfig/dryRun"/></td> </tr>
            <tr> <th>Skip validation:</th> <td><xsl:value-of select="/migrationReport/config/globalConfig/skipValidation"/></td> </tr>
            <tr> <th>Test run:</th>        <td><xsl:value-of select="/migrationReport/config/globalConfig/testRun"/></td> </tr>
        </table>
        
    </xsl:template>

    <!-- Comparison result -->
    <xsl:template name="ComparisonResult">
        <div class="box comparison">
            <a href="#" onclick="$('#comparison').slideToggle(10)">show/hide</a>
            <table class="flat data vertBorder fs90" id="comparison">
                <tr> <th colspan="2">Result</th> <th>File</th> </tr>
                <xsl:for-each select="/migrationReport/comparisonResult/matches/match[@result != 'MATCH']">
                    <tr class="match {@result}">
                        <td class="icon"><div/></td>
                        <td class="result"> <xsl:value-of select="@result"/> </td>
                        <td><xsl:value-of select="@path"/></td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

    <!-- Deployments - WindUp reports etc. -->
    <xsl:template name="Deployments">
        <div class="box deployments">
            <table class="flat data vertBorder fs90" id="deployments">
                <tr> <th colspan="2">Report</th> <th>File</th> </tr>
                <xsl:for-each select="/migrationReport/deployments/deployment">
                    <tr class="deployment {@type}">
                        <td class="icon"><div/></td>
                        <td class="report">
                            <xsl:if test="@reportDir">
                                <a href="WindUp/{@reportDir}/index.html">WindUp</a>
                            </xsl:if>
                            <xsl:if test="not( @reportDir )">(none)</xsl:if>
                        </td>
                        <td><xsl:value-of select="@path"/></td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

    <!-- Source server config data (MigratorData) -->
    <xsl:template name="MigratorData">
        <xsl:for-each select="/migrationReport/configsData/configData">
            <div class="box migratorData">
                <!-- From annotation -->
                <h4><div class="icon"/>
                    <xsl:value-of select="@name"/>
                    <xsl:if test="not(@name)"><xsl:value-of select="@fromMigrator"/></xsl:if>
                    <xsl:if test="@docLink">
                        (<a href="{@docLink}">
                            <xsl:if test="not(@docName)">Documentation</xsl:if>
                            <xsl:if test="@docName"><xsl:value-of select="@docName"/></xsl:if>
                        </a>)
                    </xsl:if>
                </h4>
                <div class="padding">
                    <table class="fragments flat vertBorder" style="border-collapse: collapse;">
                        <!-- Fragments -->
                        <xsl:for-each select="configFragments/configFragment">
                            <tr>
                                <td class="icon"><div/></td>
                                <td>
                                    <!-- From annotation -->
                                    <div class="name">
                                        <xsl:value-of select="@name"/>
                                        <xsl:if test="not(@name)"><xsl:value-of select="@fromMigrator"/></xsl:if>
                                    </div>
                                    <div class="origin">
                                        <xsl:if test="@class">
                                            <div class="class"> <div class="icon"/> Class: <code><xsl:value-of select="@class"/></code></div>
                                        </xsl:if>
                                        <xsl:if test="origin/@file">
                                            <div class="file"> <div class="icon"/> File: <code><xsl:value-of select="origin/@file"/></code></div>
                                        </xsl:if>
                                        <xsl:if test="origin/@file">
                                            <div class="part"> <div class="icon"/> Part: <code><xsl:value-of select="origin/@part"/></code></div>
                                        </xsl:if>
                                        <xsl:if test="origin/@server">
                                            <div class="server"> <div class="icon"/> Server: <code><xsl:value-of select="origin/@server"/></code></div>
                                        </xsl:if>
                                    </div>
                                    <xsl:if test="docRef">
                                        <div class="docRef">
                                            <a href="@link"><xsl:value-of select="docRef/@name"/></a>
                                        </div>
                                    </xsl:if>
                                    <xsl:if test="properties">
                                        <div class="properties">
                                            <table class="flat vertBorder">
                                                <xsl:for-each select="properties/property">
                                                <tr>
                                                    <th><xsl:value-of select="@name"/></th>
                                                    <td><xsl:value-of select="@value"/></td>
                                                </tr>
                                                </xsl:for-each>
                                            </table>
                                        </div>
                                    </xsl:if>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </div>
            </div>
        </xsl:for-each>
    </xsl:template>

    <!-- Actions -->
    <xsl:template name="Actions">
        <xsl:for-each select="/migrationReport/actions/action">
            <div class="box action {@fromMigrator}" id="{@id}">
                <a name="action{@id}"/>
                <h4><div class="icon"/>
                    <!-- from migrator <xsl:value-of select="@fromMigrator"/> -->
                    <xsl:if test="@label">
                        <xsl:value-of select="@label"/>
                    </xsl:if>
                    <xsl:if test="not( @label )">
                        <!--
                        <xsl:value-of select="substring-after( @class, '*\.')"/>
                        <xsl:value-of select="replace( @class, '\.(.*)$', '$1')"/>
                        -->
                        <xsl:value-of select="reverse(tokenize(@class,'\.'))[1]"/>
                        <xsl:if test="@fromMigrator">
                            from migrator <xsl:value-of select="@fromMigrator"/>
                        </xsl:if>
                    </xsl:if>
                        
                </h4>
                <div class="padding">
                    <p class="desc"><xsl:value-of select="desc/text()"/></p>
                    
                    <!-- Report properties -->
                    <xsl:if test="properties/property">
                        <div class="padding">
                            <xsl:for-each select="properties/property">
                                <div class="property {@style}">
                                    <div class="icon"/>
                                    <xsl:if test="@label">
                                        <div class="label"><xsl:value-of select="@label"/></div>
                                    </xsl:if>
                                    <div class="value"><xsl:value-of select="@value"/></div>
                                </div>
                            </xsl:for-each>
                        </div>
                    </xsl:if>

                    <xsl:if test="@fromMigrator">
                        <div class="fromMigrator">From migrator <xsl:value-of select="@fromMigrator"/></div>
                    </xsl:if>
                                        
                    <!-- Warnings -->
                    <xsl:if test="warnings/*">
                        <div class="padding">
                            <!--<h4>Warnings</h4>-->
                            <table class="warnings wid100p flat vertBorder">
                            <xsl:for-each select="warnings/warning">
                                <tr>
                                    <td class="icon"><div/></td> <td class="text"><xsl:value-of select="text()"/></td>
                                </tr>
                            </xsl:for-each>
                            </table>
                        </div>
                    </xsl:if>
                    
                    <!-- Dependencies -->
                    <xsl:if test="dependencies/dep">
                        <div class="padding">
                            Depends on
                            <xsl:for-each select="dependencies/dep">
                                <a href="#action{text()}"> <xsl:value-of select="position()"/> </a>
                            </xsl:for-each>
                        </div>
                    </xsl:if>
                    
                    
                    
                </div>
            </div>
        </xsl:for-each>
    </xsl:template>
    
    <!-- Catch-all template - ignore whatever is not specified above. -->
    <xsl:template match="@*|node()"/>

</xsl:stylesheet>
