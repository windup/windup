<?xml version="1.0" encoding="UTF-8"?>
<#macro processEnvRef environmentRef>
	<#switch environmentRef.referenceTagType>
	  <#case "RESOURCE_ENV_REF">
		  <resource-env-ref>
                <resource-env-ref-name>${environmentRef.name}</resource-env-ref-name>
                <#if environmentRef.jndiReference??><jndi-name>${environmentRef.jndiReference.jndiLocation}</jndi-name></#if>
         </resource-env-ref>
	     <#break>
	  <#case "RESOURCE_REF">
	  	 <resource-ref>
            <res-ref-name>${environmentRef.name}</res-ref-name>
            <#if environmentRef.jndiReference??><jndi-name>${environmentRef.jndiReference.jndiLocation}</jndi-name></#if>
         </resource-ref>
	     <#break>
	  <#case "EJB_LOCAL_REF">
	  	 <ejb-local-ref>
			<ejb-ref-name>${environmentRef.name}</ejb-ref-name>
			<#if environmentRef.jndiReference??><jndi-name>${environmentRef.jndiReference.jndiLocation}</jndi-name></#if>
		 </ejb-local-ref>
		 <#break>
	  <#case "EJB_REF">
	  	 <ejb-ref>
			<ejb-ref-name>${environmentRef.name}</ejb-ref-name>
			<#if environmentRef.jndiReference??><jndi-name>${environmentRef.jndiReference.jndiLocation}</jndi-name></#if>
		 </ejb-ref>
		 <#break>
	  <#case "MSG_DESTINATION_REF">
		 <message-destination-ref>
            <message-destination-ref-name>${environmentRef.name}</message-destination-ref-name>
            <#if environmentRef.jndiReference??><jndi-name>${environmentRef.jndiReference.jndiLocation}</jndi-name></#if>
         </message-destination-ref>
		 <#break>
	  <#default>
		<!-- Unhandled type: ${environmentRef.referenceTagType} -->
	</#switch>
</#macro>
<jboss-web>
	<#if iterableHasContent(reportModel.relatedResources.environmentReferences)>
	<#list reportModel.relatedResources.environmentReferences.list.iterator() as environmentRef>
		<@processEnvRef environmentRef />
	</#list>
    </#if>
</jboss-web>