<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    version="1.0"
    xmlns:hq="urn:hornetq"> 
    
    <xsl:output method="xml" indent="yes" 
                version="1.0" encoding="UTF-8" omit-xml-declaration="no"/>
         
    <xsl:template match="/*">
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
            
#################################################################################                       
        </xsl:comment> 
        
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:text>&#10;</xsl:text> <!-- newline --> 
        
        <xsl:comment>
#################################################################################
 
 If there are any statements between this comment and the next comment block  
 copy/paste them into the &lt;hornetq-server&gt; tag of the messaging subsystem
 of your server configuration file.                                
            
#################################################################################                       
        </xsl:comment> 
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        
        <xsl:element name="hornetq-server">
        <xsl:text>&#10;</xsl:text> <!-- newline -->    
        <xsl:if test="hq:bindings-directory">
            <xsl:element name="bindings-directory">
                <xsl:attribute name="path">
                    <xsl:value-of select="hq:bindings-directory"></xsl:value-of>
                </xsl:attribute>
            </xsl:element>
        </xsl:if>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        
        <xsl:if test="hq:journal-directory">
            <xsl:element name="journal-directory">
                <xsl:attribute name="path">
                    <xsl:value-of select="hq:journal-directory"></xsl:value-of>
                </xsl:attribute>
            </xsl:element>
        </xsl:if>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
    
        <xsl:if test="hq:large-messages-directory">
            <xsl:element name="large-messages-directory">
                <xsl:attribute name="path">
                    <xsl:value-of select="hq:large-messages-directory"></xsl:value-of>
                </xsl:attribute>
            </xsl:element>
        </xsl:if>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        
        <xsl:if test="hq:paging-directory">
            <xsl:element name="paging-directory">
                <xsl:attribute name="path">
                    <xsl:value-of select="hq:paging-directory"></xsl:value-of>
                </xsl:attribute>
            </xsl:element>
        </xsl:if>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        
        <xsl:if test="hq:journal-min-files">
            <xsl:element name="journal-min-files">
                <xsl:value-of select="hq:journal-min-files"></xsl:value-of>
            </xsl:element>
        </xsl:if>
        
        <xsl:apply-templates select="hq:connectors"/>        
        <xsl:apply-templates select="hq:acceptors"/>
        
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:comment>
#################################################################################
 
 If there are any &lt;security-setting&gt; statements below copy/paste them into
 the &lt;security-settings&gt; tag section of the messaging subsystem of your server 
 configuration file.                                
            
#################################################################################                       
        </xsl:comment>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:apply-templates select="hq:security-settings" mode="security-settings"/>
        
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:comment>
#################################################################################
 
 If there are any &lt;address-setting&gt; statements below copy/paste them into
 the &lt;address-settings&gt; tag section of the messaging subsystem of your server 
 configuration file.                                
            
#################################################################################                       
        </xsl:comment>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:apply-templates select="hq:address-settings" mode="address-settings"/>
   
        <xsl:apply-templates select="/*" mode="excerptSocketBinding"/>
        </xsl:element>    
    </xsl:template>
             
    
    <xsl:template match="hq:connectors">
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:text>&#10;</xsl:text> <!-- newline --> 
        <xsl:comment>
#################################################################################
 
 If there are any &lt;connector&gt; statements below copy/paste them into
 the &lt;connectors&gt; tag section of the messaging subsystem of your server 
 configuration file.                                
            
#################################################################################                       
        </xsl:comment> 
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:element name="connectors">
            <xsl:for-each select="hq:connector">
                <xsl:if test="not(contains('|netty|netty-throughput|in-vm|', concat('|', @name, '|')))">
                    <xsl:element name="connector">
                        <xsl:attribute name="name">
                            <xsl:value-of select="@name" />
                        </xsl:attribute>
                                
                        <xsl:if test="./hq:param[@key='port']">
                            <xsl:attribute name="socket-binding">
                                <xsl:text>msg-</xsl:text>
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                        </xsl:if>
                                
                        <xsl:if test="hq:factory-class">
                            <xsl:element name="factory-class">
                                <xsl:value-of select="hq:factory-class"/>
                            </xsl:element>
                        </xsl:if>
                                
                        <!-- skip host and port these are part of the socket-binding def -->
                        <xsl:for-each select="hq:param">
                            <xsl:if test="not(contains('|host|port|', concat('|',@key, '|')))">                                        
                                <xsl:element name="param">
                                    <xsl:attribute name="key">
                                        <xsl:value-of select="@key"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="@value"/>
                                    </xsl:attribute>
                                </xsl:element>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:if>
            </xsl:for-each>
        </xsl:element>    
    </xsl:template>
                

    <!-- -->
    <xsl:template match="hq:acceptors"> 
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:comment>
#################################################################################
 
 If there are any &lt;acceptor&gt; statements below copy/paste them into
 the &lt;acceptors&gt; tag section of the messaging subsystem of your server 
 configuration file.                                
            
#################################################################################                       
        </xsl:comment>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:element name="acceptors">
            <xsl:for-each select="hq:acceptor">
                <xsl:if test="not(contains('|netty|netty-throughput|in-vm|', concat('|', @name, '|')))">
                    <xsl:element name="acceptor">
                        <xsl:attribute name="name">
                            <xsl:value-of select="@name" />
                        </xsl:attribute>
                                
                        <xsl:if test="./hq:param[@key='port']">
                            <xsl:attribute name="socket-binding">
                                <xsl:text>msg-</xsl:text>
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                        </xsl:if>
                                
                        <xsl:if test="hq:factory-class">
                            <xsl:element name="factory-class">
                                <xsl:value-of select="hq:factory-class"/>
                            </xsl:element>
                        </xsl:if>
                                
                        <!-- skip host and port these are part of the socket-binding def -->
                        <xsl:for-each select="hq:param">
                            <xsl:if test="not(contains('|host|port|', concat('|',@key, '|')))">                                        
                                <xsl:element name="param">
                                    <xsl:attribute name="key">
                                        <xsl:value-of select="@key"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="@value"/>
                                    </xsl:attribute>
                                </xsl:element>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:if>
            </xsl:for-each>
        </xsl:element> 
    </xsl:template>
 
        
    <xsl:template match="/*" mode="excerptSocketBinding"> 
        <xsl:text>&#10;</xsl:text> <!-- newline --> 
        <xsl:text>&#10;</xsl:text> <!-- newline -->      
        <xsl:comment>
#################################################################################
 
 If &lt;socket-binding&gt; statements were generated (below), review
 them; remove the duplicates and copy/paste the remaining into the 
 &lt;socket-binding-group&gt; tag of your server configuration file.                       
            
#################################################################################                       
        </xsl:comment>
        <xsl:text>&#10;</xsl:text> <!-- newline -->
        <xsl:apply-templates select="hq:connectors" mode="connectorSocketBinding"/>
        <xsl:apply-templates select="hq:acceptors" mode="acceptorSocketBinding"/>
    </xsl:template>
    
    <xsl:template match="hq:connectors" mode="connectorSocketBinding">
        
        <xsl:for-each select="hq:connector">
                <xsl:if test="not(contains('|netty|netty-throughput|in-vm|', concat('|', @name, '|')))">
                    <xsl:if test="./hq:param[@key='port']">
                        <xsl:element name="socket-binding">
                            <xsl:attribute name="name">
                                <xsl:text>msg-</xsl:text>
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                                
                            <xsl:attribute name="port">
                                <xsl:value-of select="./hq:param[@key='port']/@value"/>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:text>&#10;</xsl:text> <!-- newline -->
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
    </xsl:template> 
    
    <xsl:template match="hq:acceptors" mode="acceptorSocketBinding">
        
        <xsl:for-each select="hq:acceptor">
                <xsl:if test="not(contains('|netty|netty-throughput|in-vm|', concat('|', @name, '|')))">
                    <xsl:if test="./hq:param[@key='port']">
                        <xsl:element name="socket-binding">
                            <xsl:attribute name="name">
                                <xsl:text>msg-</xsl:text>
                                <xsl:value-of select="@name"/>
                            </xsl:attribute>
                                
                            <xsl:attribute name="port">
                                <xsl:value-of select="./hq:param[@key='port']/@value"/>
                            </xsl:attribute>
                        </xsl:element>
                        <xsl:text>&#10;</xsl:text> <!-- newline -->
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
    </xsl:template>  
    
    <xsl:template match="hq:security-settings" mode="security-settings">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*" />
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="hq:address-settings" mode="address-settings">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*" />
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
                                            
 </xsl:stylesheet>        
