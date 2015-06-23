<?xml version="1.1" encoding="UTF-8"?>
<jboss:ejb-jar xmlns:jboss="http://www.jboss.com/xml/ns/javaee"
               xmlns="http://java.sun.com/xml/ns/javaee"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:s="urn:security:1.1"
               xsi:schemaLocation="http://www.jboss.com/xml/ns/javaee http://www.jboss.org/j2ee/schema/jboss-ejb3-2_0.xsd http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd"
               version="3.1"
               impl-version="2.1">
    <enterprise-beans>
		
		<#if iterableHasContent(reportModel.relatedResources.sessionBeans)>
		<#list reportModel.relatedResources.sessionBeans.list.iterator() as sessionBean>
		<session> 
            <ejb-name>${sessionBean.beanName}</ejb-name>
            <session-type>Stateless</session-type>
            
            <#if iterableHasContent(sessionBean.environmentReferences)>
	            <#list sessionBean.environmentReferences.iterator() as environmentRef>
		            <#switch environmentRef.referenceTagType>
					  <#case "RESOURCE_REF">
					  	 <resource-ref>
			                <res-ref-name>${environmentRef.name}</res-ref-name>
			                <#if environmentRef.jndiReference??><jndi-name>${environmentRef.jndiReference.jndiLocation}</jndi-name></#if>
			             </resource-ref>
					     <#break>
					  <#case "EJB_LOCAL_REF">
					  	 <ejb-local-ref>
							<ejb-ref-name>${environmentRef.name}</ejb-ref-name>
							<#if environmentRef.jndiReference??><mapped-name>${environmentRef.jndiReference.jndiLocation}</mapped-name></#if>
						 </ejb-local-ref>
						 <#break>
					  <#default>
						<!-- Unhandled type: ${environmentRef.referenceTagType} -->
					</#switch>
	            </#list>
            </#if>
        </session>
        </#list>
        </#if>
        
     	<#if reportModel.relatedResources.messageDriven.list.iterator()?has_content>
    	<#list reportModel.relatedResources.messageDriven.list.iterator() as mdb>
    	<message-driven>
			<ejb-name>${mdb.beanName}</ejb-name>
			<activation-config>
               <activation-config-property>
               	  <#if mdb.destination??>
                  <activation-config-property-name>destination</activation-config-property-name>
                  <activation-config-property-value>${mdb.destination.jndiLocation}</activation-config-property-value>
                  </#if>
               </activation-config-property>
            </activation-config>
    	</message-driven>
    	</#list>
    	</#if>
    </enterprise-beans>
</jboss:ejb-jar>