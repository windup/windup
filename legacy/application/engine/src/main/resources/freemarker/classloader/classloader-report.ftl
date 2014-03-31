
	<#if (classpathIssues?size > 0)>
		<div class='windupBody'>
			<div class="windupHighLevel">
				
				<#list classpathIssues?keys as missing>
					<h3>${missing}</h2>
					<ul>
						<#list classpathIssues[missing]	 as issue>
							
						<li>${issue}</li>
						
						</#list>
					</ul>
				</#list>
				
			</div>
		</div>
	</#if>

	<#include "classloader-script.ftl">
