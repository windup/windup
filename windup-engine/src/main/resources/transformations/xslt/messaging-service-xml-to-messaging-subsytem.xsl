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
            
 If there are no address-setting tags defined below no further action is needed.
 If there are address-setting tags, make the needed edits to the XML and then
 cut/paste it into the "address-settings" tag in the server configuration file.
 If there is no "address-settings" tag in the server configuration file messaging
 subsystem cut/past the full contents here.                  
                                     
#################################################################################
        </xsl:comment>
        
        <address-settings>
            <xsl:for-each select="mbean">
                <xsl:comment> ======================================
       Check and adjust the 'match' attribute.
       ====================================== </xsl:comment>
       
                <address-setting match="#">
                    <!-- -->
                    <xsl:if test="attribute[@name='DefaultDLQ']">
                        <xsl:choose>
                            <xsl:when test="not(attribute[@name='DefaultDLQ'] = 'jboss.messaging.destination:service=Queue,name=DLQ')" >
                                <xsl:element name="dead-letter-address">
                                    <xsl:value-of
                                        select="attribute[@name='DefaultDLQ']"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:if>

                    <xsl:if test="attribute[@name='DefaultExpiryQueue']">
                        <xsl:choose>
                            <xsl:when test="not(attribute[@name='DefaultExpiryQueue'] = 'jboss.messaging.destination:service=Queue,name=ExpiryQueue')" >
                                <xsl:element name="expiry-address">
                                    <xsl:value-of
                                        select="attribute[@name='DefaultExpiryQueue']"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:if>

                    <!-- change this -->
                    <xsl:if test="attribute[@name='DefaultRedeliveryDelay']">
                        <xsl:choose>
                            <xsl:when test="not(attribute[@name='DefaultRedeliveryDelay'] = '0')" >
                                <xsl:element name="redelivery-delay">
                                    <xsl:value-of
                                        select="attribute[@name='DefaultRedeliveryDelay']"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:if>

                    <xsl:if test="attribute[@name='DefaultMaxDeliveryAttempts']">
                        <xsl:choose>
                            <xsl:when test="not(attribute[@name='DefaultMaxDeliveryAttempts'] = 10)" >
                                <xsl:element name="max-delivery-attempts">
                                    <xsl:value-of
                                        select="attribute[@name='DefaultMaxDeliveryAttempts']"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:if>
                </address-setting>

            </xsl:for-each>
        </address-settings>
              
    </xsl:template>
</xsl:stylesheet>