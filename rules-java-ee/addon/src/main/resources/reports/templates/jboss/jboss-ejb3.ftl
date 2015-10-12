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
<#macro processPool bean>
	<#if bean.threadPool??>
		<p:pool>
			<ejb-name>${bean.beanName}</ejb-name>
			<p:bean-instance-pool-ref>${bean.threadPool.poolName}</p:bean-instance-pool-ref>
        </p:pool>
	</#if>
</#macro>
<jboss:ejb-jar xmlns:jboss="http://www.jboss.com/xml/ns/javaee"
               xmlns="http://java.sun.com/xml/ns/javaee"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xmlns:s="urn:security:1.1"
               xmlns:tx="urn:trans-timeout"
               xmlns:c="urn:clustering:1.0"  
               xmlns:p="urn:ejb-pool:1.0"
               xsi:schemaLocation="http://www.jboss.com/xml/ns/javaee http://www.jboss.org/j2ee/schema/jboss-ejb3-2_0.xsd http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd
               urn:trans-timeout http://www.jboss.org/j2ee/schema/trans-timeout-1_0.xsd"
               version="3.1"
               impl-version="2.1">
               
    <enterprise-beans>
		
		<#if iterableHasContent(reportModel.relatedResources.sessionBeans)>
		<#list reportModel.relatedResources.sessionBeans.list.iterator() as sessionBean>
		<session> 
            <ejb-name>${sessionBean.beanName}</ejb-name>
            <session-type>${sessionBean.sessionType}</session-type>
            
            <#if iterableHasContent(sessionBean.environmentReferences)>
	            <#list sessionBean.environmentReferences.iterator() as environmentRef>
		            <@processEnvRef environmentRef />
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
			   <#if mdb.destination??>
               <activation-config-property>
                  <activation-config-property-name>destination</activation-config-property-name>
                  <activation-config-property-value>${mdb.destination.jndiLocation}</activation-config-property-value>
               </activation-config-property>
               </#if>
            </activation-config>
            
            <#if iterableHasContent(mdb.environmentReferences)>
	            <#list mdb.environmentReferences.iterator() as environmentRef>
		            <@processEnvRef environmentRef />
	            </#list>
            </#if>
    	</message-driven>
    	</#list>
    	</#if>
    </enterprise-beans>
   <assembly-descriptor>   
     <#if iterableHasContent(reportModel.relatedResources.sessionBeans)>
         <#list reportModel.relatedResources.sessionBeans.list.iterator() as sessionBean>
                 <@processTxTimeout sessionBean />
		  <#if sessionBean.clustered?? && sessionBean.clustered>
		      <c:clustering>
		          <ejb-name>${sessionBean.beanName}</ejb-name>  
		          <c:clustered>true</c:clustered>  
   		      </c:clustering>  
		  </#if>
         </#list>
    </#if>
    <#if reportModel.relatedResources.messageDriven.list.iterator()?has_content>
    	<#list reportModel.relatedResources.messageDriven.list.iterator() as mdb>
    	        <@processTxTimeout mdb />
    	        <@processPool mdb />
         </#list>
    </#if>	
    	
   </assembly-descriptor>


</jboss:ejb-jar>