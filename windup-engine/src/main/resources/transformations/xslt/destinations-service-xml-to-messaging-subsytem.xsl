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
 for a file that contains, &lt;extension module="org.jboss.as.messaging"/&gt; 
 Select one of those files to edit and use to run the server.  As of this 
 writing these files have the messaging subsystem defined,

            $JBOSS_HOME/standalone/configuration/standalone-full.xml
            $JBOSS_HOME/standalone/configuration/standalone-full-ha.xml
            $JBOSS_HOME/docs/examples/configs/standalone-hornetq-colocated.xml
            $JBOSS_HOME/docs/examples/configs/standalone-xts.xml
  
 Search for the subsystem definition, with attribute &lt;subsystem xmlns="urn:jboss:domain:messaging:1.3"&gt;
 (Note, the slot number might be something other than 1.3.).  
            
 If there are no jms-queue or jms-topic tags defined below no further action is needed.
 If there are jms-queue or jms-topic tags, make the needed edits to the XML and then
 cut/paste it into the "jms-destinations" tag in the server configuration file.
 If there is no "jms-destinations" tag in the server configuration file messaging
 subsystem cut/past the full contents here.                  
                                     
 #################################################################################
        </xsl:comment>
        
        <jms-destinations>
            <xsl:for-each select="mbean">
        
                <xsl:if test="@code='org.jboss.jms.server.destination.QueueService'">
                    <xsl:element name='jms-queue'>
                        <xsl:attribute name="name">
                            <xsl:value-of select="substring-after(@name,'name=')"/> 
                        </xsl:attribute>
                        <xsl:comment> ### &lt;entry name='JNDI_NAME'/&gt; ### </xsl:comment>                        
                    </xsl:element>
                </xsl:if> 
             
                <xsl:if test="@code='org.jboss.jms.server.destination.TopicService'">
                    <xsl:element name='jms-topic'>
                        <xsl:attribute name="name">
                            <xsl:value-of select="substring-after(@name,'name=')"/>
                        </xsl:attribute>
                        <xsl:comment> ### &lt;entry name='JNDI_NAME'/&gt; ### </xsl:comment>
                    </xsl:element>
                </xsl:if>  
            </xsl:for-each> 
        </jms-destinations>     
    </xsl:template>
</xsl:stylesheet>     