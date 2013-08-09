<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="xml" indent="yes"
                version="1.0" encoding="UTF-8" omit-xml-declaration="no"/>

    <xsl:template match="/server">
        <xsl:comment>
#################################################################################
last edited: 8/1/2013
                        
 The user needs to take action to finish configuring the HornetQ
 messaging subsystem in the JBoss server,(EAP6, AS7, WildFly).
    
 The default server configuration files standalone.xml or domain.xml do not
 have the messaging subsystem defined, however the server ships with configuration
 files that do. Look in $JBOSS_HOME/domain/configuration,
 $JBOSS_HOME/standalone/configuration, $JBOSS_HOME/docs/examples/configs
 for a file that contains, &lt;extension module="org.jboss.as.messaging"/&gt;.  
 Select one of those files to edit and use to run the server.  As of this 
 writing these files have the messaging subsystem defined,
 
           $JBOSS_HOME/standalone/configuration/standalone-full.xml
           $JBOSS_HOME/standalone/configuration/standalone-full-ha.xml
           $JBOSS_HOME/docs/examples/configs/standalone-hornetq-colocated.xml
           $JBOSS_HOME/docs/examples/configs/standalone-xts.xml
  
 Search for the subsystem definition, &lt;subsystem xmlns="urn:jboss:domain:messaging:1.3"&gt;
 (Note, the slot number might be something other than 1.3.).  
            
 If there are no connection-factory tags defined below no further action is needed.
 If there are connection-factory tags, make the needed edits to the XML and 
 then cut/paste it into the "jms-connection-factory" tag in the server configuration 
 file.       
             
    The user MUST provide the approriate values for these variables.
        ##-PROVIDE_A_UNIQUE_FACTORY_NAME-##     ; This name must be unique within 
                the file.
        ##-REF_NAME_OF_THE_CONNECTION_FACTORY-##    ;The standard values are 
                (netty | in-vm).
            netty connections factories can be used by a remote client.
            in-vm connection factories can be used by a local client (i.e. one 
                running in the same JVM as the server)
 
                        
 ref: https://docs.jboss.org/author/display/AS72/Messaging+configuration
#################################################################################
        </xsl:comment>
                       
        <!-- Report non-default mbeans -->
        <xsl:for-each select="mbean">
            <xsl:if test="not(@name='jboss.messaging.connectionfactory:service=ConnectionFactory') and not(@name='jboss.messaging.connectionfactory:service=ClusteredConnectionFactory') and not(@name='jboss.messaging.connectionfactory:service=ClusterPullConnectionFactory')">
               
                <connection-factory name="##-PROVIDE_A_UNIQUE_FACTORY_NAME-##">
                    <!-- Connector -->
                    <connectors> 
                        <connector-ref>##-REF_ID_OF_CONNECTOR-##</connector-ref>
                    </connectors>
                    
                    <!-- -->
                    <entries>
                        <xsl:for-each select="attribute[@name='JNDIBindings']/bindings//binding">                                     
                            <entry>
                                <xsl:attribute name="name">
                                    <xsl:value-of select="."/>
                                </xsl:attribute>
                            </entry>
                        </xsl:for-each>
                    </entries>
                    
                    
                    <!-- -->
                    <xsl:if test="constructor/arg[@type='java.lang.String']">
                        <client-id>
                            <xsl:value-of select="constructor/arg/@value"/>
                        </client-id>
                    </xsl:if>
                    
                    <xsl:if test="attribute[@name='SendAcksAsync']">
                        <xsl:element name="block-on-acknowledge">
                           <xsl:value-of select="attribute[@name='SendAcksAsync']"/> 
                        </xsl:element>
                    </xsl:if>
                
                    <xsl:if test="attribute[@name='PrefetchSize']">
                        <xsl:element name="consumer-window-size">
                            <xsl:value-of select="attribute[@name='PrefetchSize']"/>
                        </xsl:element>
                    </xsl:if>
                   
                    <xsl:if test="attribute[@name='DupsOKBatchSize']">
                        <xsl:element name="dups-ok-batch-size">
                            <xsl:value-of select="attribute[@name='DupsOKBatchSize']"/>
                        </xsl:element>
                    </xsl:if>
                    
                    
                    <xsl:if test="attribute[@name='LoadBalancingFactory']">
                        <xsl:element name="connection-load-balancing-policy-class-name">
                            <xsl:value-of select="attribute[@name='LoadBalancingFactory']"/>
                        </xsl:element>
                    </xsl:if>
                    
                </connection-factory>
            </xsl:if>
        </xsl:for-each> 
        
    </xsl:template>
 </xsl:stylesheet>
