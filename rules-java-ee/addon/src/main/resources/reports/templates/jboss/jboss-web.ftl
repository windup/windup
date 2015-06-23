<?xml version="1.0" encoding="UTF-8"?>
<jboss-web>
	<#if iterableHasContent(reportModel.relatedResources.environmentReferences)>
	<#list reportModel.relatedResources.environmentReferences.list.iterator() as environmentRef>
		<#switch environmentRef.referenceTagType>
			<#case "EJB_REF">
				<ejb-ref>
					<ejb-ref-name>ejb/remote/UserServiceRemote</ejb-ref-name>
					<jndi-name>java:global/sample-app-ear-1.0.0/sample-app-ejb-1.0.0/UserService!com.rhc.booking.services.UserServiceRemote</jndi-name>
				</ejb-ref>
			<#break>
			<#case "EJB_LOCAL_REF">
				<ejb-local-ref>
					<ejb-ref-name>ejb/local/NotificationServiceLocal</ejb-ref-name>
					<jndi-name>java:global/sample-app-ear-1.0.0/sample-app-ejb-1.0.0/NotificationService!com.rhc.booking.services.NotificationServiceLocal</jndi-name>
				</ejb-local-ref>
			<#break>
			<#case "RESOURCE_REF">
				<resource-ref>
					<res-ref-name>jms/NotificationConnectionFactory</res-ref-name>
					<jndi-name>java:/ConnectionFactory</jndi-name>
				</resource-ref>
			<#break>
			<#case "MSG_DESTINATION_REF">
				<message-destination-ref>
					<message-destination-ref-name>jms/NotificationQueue</message-destination-ref-name>
					<jndi-name>java:/jms/queue/NotificationQueue</jndi-name>
				</message-destination-ref>
			<#break>

			<#default>
			<!-- Unhandled type: ${environmentRef.referenceTagType} -->
		</#switch>
	</#list>
    </#if>
</jboss-web>