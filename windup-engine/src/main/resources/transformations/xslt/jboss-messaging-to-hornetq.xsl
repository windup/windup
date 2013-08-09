<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="xml" indent="yes"
                version="1.0" encoding="UTF-8" omit-xml-declaration="no"/>

    <xsl:template
        match="/server/mbean[@name='jboss.messaging:service=ServerPeer']">

        <configuration xmlns="urn:hornetq"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:schemaLocation="urn:hornetq /schema/hornetq-configuration.xsd">

            <!--  Don't change this name.
                  This is used by the dependency framework on the deployers,
                  to make sure this deployment is done before any other deployment -->
            <name>HornetQ.main.config</name>

            <log-delegate-factory-class-name>
                org.hornetq.integration.logging.Log4jLogDelegateFactory
            </log-delegate-factory-class-name>

            <bindings-directory>${jboss.server.data.dir}/hornetq/bindings
            </bindings-directory>

            <journal-directory>${jboss.server.data.dir}/hornetq/journal
            </journal-directory>

            <journal-min-files>10</journal-min-files>

            <large-messages-directory>
                ${jboss.server.data.dir}/hornetq/largemessages
            </large-messages-directory>

            <paging-directory>${jboss.server.data.dir}/hornetq/paging
            </paging-directory>



            <xsl:if test="attribute[@name='MessageCounterSamplePeriod']">
                <xsl:choose>
                    <xsl:when test="not(attribute[@name='MessageCounterSamplePeriod'] = 5000)" >
                        <xsl:comment>Value from JBoss Messaging MessageCounterSamplePeriod</xsl:comment>
                        <xsl:element name="message-counter-sample-period">
                            <xsl:value-of
                                select="attribute[@name='MessageCounterSamplePeriod']"/>
                        </xsl:element>
                    </xsl:when>
                </xsl:choose>
            </xsl:if>

            <xsl:if test="attribute[@name='EnableMessageCounters']">
                <xsl:choose>
                    <xsl:when test="not(attribute[@name='EnableMessageCounters'] = 'false')" >
                        <xsl:comment>Value from JBoss Messaging EnableMessageCounters</xsl:comment>
                        <xsl:element name="message-counter-enabled">
                            <xsl:value-of
                                select="attribute[@name='EnableMessageCounters']"/>
                        </xsl:element>
                    </xsl:when>
                </xsl:choose>
            </xsl:if>

            <xsl:if test="attribute[@name='SuckerPassword']">
                <xsl:comment>Value from JBoss Messaging SuckerPassword</xsl:comment>
                <xsl:element name="cluster-password">
                    <xsl:value-of select="attribute[@name='SuckerPassword']"/>
                </xsl:element>
            </xsl:if>


            <xsl:choose>
                <xsl:when
                    test="attribute[@name='SuckerConnectionRetryTimes'] or attribute[@name='SuckerConnectionRetryInterval']">
                    <xsl:comment>Value from JBoss Messaging SuckerConnectionRetryTimes or SuckerConnectionRetryInterval</xsl:comment>
                    <xsl:element name="bridges">
                        <xsl:element name="bridge">
                            <xsl:if
                                test="attribute[@name='SuckerConnectionRetryTimes']">
                                <xsl:element name="reconnect-attempts">
                                    <xsl:value-of
                                        select="attribute[@name='SuckerConnectionRetryTimes']"/>
                                </xsl:element>
                            </xsl:if>

                            <xsl:if
                                test="attribute[@name='SuckerConnectionRetryInterval']">
                                <xsl:element name="reconnect-interval">
                                    <xsl:value-of
                                        select="attribute[@name='SuckerConnectionRetryInterval']"/>
                                </xsl:element>
                            </xsl:if>

                        </xsl:element>
                    </xsl:element>

                </xsl:when>
            </xsl:choose>


            <connectors>

                <connector name="netty">
                    <factory-class>
                        org.hornetq.core.remoting.impl.netty.NettyConnectorFactory
                    </factory-class>
                    <param key="host"
                           value="${{jboss.bind.address:localhost}}"/>
                    <param key="port"
                           value="${{hornetq.remoting.netty.port:5445}}"/>
                </connector>


                <connector name="netty-throughput">
                    <factory-class>
                        org.hornetq.core.remoting.impl.netty.NettyConnectorFactory
                    </factory-class>
                    <param key="host"
                           value="${{jboss.bind.address:localhost}}"/>
                    <param key="port"
                           value="${{hornetq.remoting.netty.batch.port:5455}}"/>
                    <param key="batch-delay" value="50"/>
                </connector>

                <connector name="in-vm">
                    <factory-class>
                        org.hornetq.core.remoting.impl.invm.InVMConnectorFactory
                    </factory-class>
                    <param key="server-id" value="${{hornetq.server-id:0}}"/>
                </connector>

            </connectors>

            <acceptors>
                <acceptor name="netty">
                    <factory-class>
                        org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory
                    </factory-class>
                    <param key="host"
                           value="${{jboss.bind.address:localhost}}"/>
                    <param key="port"
                           value="${{hornetq.remoting.netty.port:5445}}"/>
                </acceptor>

                <acceptor name="netty-throughput">
                    <factory-class>
                        org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory
                    </factory-class>
                    <param key="host"
                           value="${{jboss.bind.address:localhost}}"/>
                    <param key="port"
                           value="${{hornetq.remoting.netty.batch.port:5455}}"/>
                    <param key="batch-delay" value="50"/>
                    <param key="direct-deliver" value="false"/>
                </acceptor>

                <acceptor name="in-vm">
                    <factory-class>
                        org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory
                    </factory-class>
                    <param key="server-id" value="0"/>
                </acceptor>

            </acceptors>

            <security-settings>
                <security-setting match="#">
                    <permission type="createNonDurableQueue" roles="guest"/>
                    <permission type="deleteNonDurableQueue" roles="guest"/>
                    <permission type="consume" roles="guest"/>
                    <permission type="send" roles="guest"/>
                </security-setting>
            </security-settings>

            <address-settings>

                <address-setting match="#">

                    <!-- -->

                    <xsl:if test="attribute[@name='DefaultDLQ']">
                    <xsl:choose>
                        <xsl:when test="(attribute[@name='DefaultDLQ'] = 'jboss.messaging.destination:service=Queue,name=DLQ')" >
                            <dead-letter-address>jms.queue.DLQ</dead-letter-address>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:comment>Value from JBoss Messaging DefaultDLQ</xsl:comment>
                            <xsl:element name="dead-letter-address">
                                <xsl:value-of
                                    select="attribute[@name='DefaultDLQ']"/>
                            </xsl:element>
                        </xsl:otherwise>
                    </xsl:choose>
                    </xsl:if>

                    <xsl:if test="attribute[@name='DefaultExpiryQueue']">
                        <xsl:choose>
                            <xsl:when test="(attribute[@name='DefaultExpiryQueue'] = 'jboss.messaging.destination:service=Queue,name=ExpiryQueue')" >
                                <expiry-address>jms.queue.ExpiryQueue</expiry-address>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:comment>Value from JBoss Messaging DefaultExpiryQueue</xsl:comment>
                                <xsl:element name="expiry-address">
                                    <xsl:value-of
                                        select="attribute[@name='DefaultExpiryQueue']"/>
                                </xsl:element>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>

                    <!-- change this -->
                    <xsl:if test="attribute[@name='DefaultRedeliveryDelay']">
                        <xsl:comment>Value from JBoss Messaging DefaultRedeliveryDelay</xsl:comment>
                        <xsl:element name="redelivery-delay">
                            <xsl:value-of
                                select="attribute[@name='DefaultRedeliveryDelay']"/>
                        </xsl:element>
                    </xsl:if>

                    <xsl:if test="attribute[@name='DefaultMaxDeliveryAttempts']">
                        <xsl:choose>
                            <xsl:when test="not(attribute[@name='DefaultMaxDeliveryAttempts'] = 10)" >
                                <xsl:comment>Value from JBoss Messaging DefaultMaxDeliveryAttempts</xsl:comment>
                                <xsl:element name="max-delivery-attempts">
                                    <xsl:value-of
                                        select="attribute[@name='DefaultMaxDeliveryAttempts']"/>
                                </xsl:element>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:if>

                        <!-- required settings -->
                     <max-size-bytes>10485760</max-size-bytes>
                     <message-counter-history-day-limit>10</message-counter-history-day-limit>
                     <address-full-policy>BLOCK</address-full-policy>
                     <redistribution-delay>60000</redistribution-delay>

                </address-setting>

            </address-settings>

        </configuration>

    </xsl:template>
 </xsl:stylesheet>