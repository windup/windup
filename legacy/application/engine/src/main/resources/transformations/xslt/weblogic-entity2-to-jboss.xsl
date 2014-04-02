<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" indent="yes" doctype-system="http://www.jboss.org/j2ee/dtd/jbosscmp-jdbc_4_0.dtd"
        doctype-public="-//JBoss//DTD JBOSSCMP-JDBC 4.0//EN"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="/">
        <xsl:apply-templates select="weblogic-rdbms-jar"/>
    </xsl:template>



    <xsl:template match="weblogic-rdbms-jar">
        <!-- <xsl:element name="defaults"> <datasource>java:/DefaultDS</datasource> <datasource-mapping>Hypersonic SQL</datasource-mapping> 
            <create-table>true</create-table> <remove-table>false</remove-table> <read-only>false</read-only> <read-time-out>300000</read-time-out> 
            <pk-constraint>true</pk-constraint> <fk-constraint>false</fk-constraint> <row-locking>false</row-locking> <preferred-relation-mapping>foreign-key</preferred-relation-mapping> 
            <read-ahead> <strategy>on-load</strategy> <page-size>1000</page-size> <eager-load-group>*</eager-load-group> </read-ahead> 
            <list-cache-max>1000</list-cache-max> <xsl:if test="./create-default-tables/text() != 'Disabled'"> <xsl:element name="create-table">true</xsl:element> 
            </xsl:if> </xsl:element> -->


        <xsl:element name="jbosscmp-jdbc">
            <xsl:element name="defaults">
                <xsl:choose>
                    <!-- http://docs.oracle.com/cd/E13222_01/wls/docs81/ejb/DDreference-cmp-jar.html#1162249 -->

                    <!-- CreateOnly: The EJB container automatically generates the table upon detecting changed schema. The 
                        container attempts to create the table based on information found in the deployment files and in the bean class. If table 
                        creation fails, a 'Table Not Found' error is thrown, and the user must create the table manually. -->
                    <xsl:when test="create-default-dbms-tables = 'CreateOnly'">
                        <xsl:element name="create-table">
                            true
                        </xsl:element>
                        <xsl:element name="alter-table">
                            false
                        </xsl:element>
                        <xsl:element name="remove-table">
                            false
                        </xsl:element>
                    </xsl:when>

                    <!-- DropAndCreate: The EJB container automatically generates the table upon detecting changed schema. 
                        The container drops and creates the table during deployment if columns have changed. The container does not save data. -->
                    <xsl:when test="create-default-dbms-tables = 'DropAndCreate'">
                        <xsl:element name="create-table">
                            true
                        </xsl:element>
                        <xsl:element name="alter-table">
                            true
                        </xsl:element>
                        <xsl:element name="remove-table">
                            false
                        </xsl:element>
                    </xsl:when>

                    <!-- DropAndCreateAlways: The EJB container automatically generates the table upon detecting changed 
                        schema. The container drops and creates the table during deployment whether or not columns have changed. The container does 
                        not save the data. -->
                    <xsl:when test="create-default-dbms-tables = 'DropAndCreateAlways'">
                        <xsl:element name="create-table">
                            true
                        </xsl:element>
                        <xsl:element name="alter-table">
                            true
                        </xsl:element>
                        <xsl:element name="remove-table">
                            true
                        </xsl:element>
                    </xsl:when>

                    <!-- AlterOrCreate: The EJB container automatically generates the table upon detecting changed schema. 
                        The container creates the table if it does not yet exist. If the table does exist, the container alters the table schema. 
                        Table data is saved. -->
                    <xsl:when test="create-default-dbms-tables = 'AlterOrCreate'">
                        <xsl:element name="create-table">
                            true
                        </xsl:element>
                        <xsl:element name="alter-table">
                            true
                        </xsl:element>
                        <xsl:element name="remove-table">
                            false
                        </xsl:element>
                    </xsl:when>

                    <!-- JBoss Defaults <xsl:otherwise> <xsl:element name="create-table">false</xsl:element> <xsl:element 
                        name="alter-table">false</xsl:element> <xsl:element name="remove-table">false</xsl:element> </xsl:otherwise> -->
                </xsl:choose>

            </xsl:element>
            <xsl:element name="enterprise-beans">
                <xsl:apply-templates select="weblogic-rdbms-bean"/>
            </xsl:element>
            <xsl:if test="weblogic-rdbms-relation">
                <xsl:element name="relationships">
                    <xsl:apply-templates select="weblogic-rdbms-relation"/>
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template match="weblogic-rdbms-bean">
        <xsl:element name="entity">
            <xsl:element name="ejb-name">
                <xsl:value-of select="./ejb-name"/>
            </xsl:element>
            <xsl:element name="datasource">
                java:/
                <xsl:value-of select="data-source-name"/>
            </xsl:element>
            <xsl:element name="datasource-mapping">
                TODO: Replace with Mapping
            </xsl:element>

            <xsl:apply-templates select="table-name"/>
            <xsl:apply-templates select="field-map"/>
            <xsl:apply-templates select="table-map"/>
            <xsl:apply-templates select="weblogic-query"/>
            <xsl:apply-templates select="automatic-key-generation"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="table-name">
        <xsl:element name="table-name">
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="table-map">
        <xsl:apply-templates select="table-name"/>
        <xsl:apply-templates select="field-map"/>
    </xsl:template>

    <xsl:template match="automatic-key-generation">
        <xsl:element name="entity-command">
            <xsl:attribute name="name">oracle-sequence</xsl:attribute>
            <xsl:element name="attribute">
                <xsl:attribute name="name">sequence</xsl:attribute>
                <xsl:value-of select="generator-name"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="field-map">
        <xsl:element name="cmp-field">
            <xsl:element name="field-name">
                <xsl:value-of select="cmp-field"/>
            </xsl:element>
            <xsl:element name="column-name">
                <xsl:value-of select="dbms-column"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="weblogic-query">
        <xsl:element name="query">
            <xsl:element name="query-method">
                <xsl:comment>
                    <xsl:value-of select="description"/>
                </xsl:comment>
                <xsl:copy-of select="query-method/method-name"/>
                <xsl:copy-of select="query-method/method-params"/>
            </xsl:element>
            <xsl:element name="jboss-ql">
                <xsl:choose>
                    <xsl:when test="contains(weblogic-ql,'ORDERBY')">
                        <xsl:value-of
                            select="concat( 
							substring-before(weblogic-ql,'ORDERBY'), 
							'ORDER BY', 
							substring-after(weblogic-ql,'ORDERBY') 
							)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="weblogic-ql"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:element>
    </xsl:template>


    <xsl:template match="weblogic-rdbms-relation">
        <xsl:element name="ejb-relation">
            <xsl:comment>
                TODO: Complete ejb relationship
            </xsl:comment>
            <xsl:element name="ejb-relation-name">
                <xsl:value-of select="relation-name"/>
            </xsl:element>
            <xsl:element name="ejb-relationship-role">
                <xsl:element name="ejb-relationship-role-name"/>
                <xsl:element name="key-fields"/>
            </xsl:element>
            <xsl:element name="ejb-relationship-role">
                <xsl:element name="ejb-relationship-role-name"/>
                <xsl:element name="key-fields"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
