
	<#if (globalResults?size > 0 || lineNumberSuggestions?size > 0 || classificationResults?size > 0 || linkResults?size > 0)>
		<div class='windupBody'>

		<div class='windupHighLevel'>
		<#if (classificationResults?size > 0)>

			<h3>Classification</h3>
			<ul>
				<#list classificationResults as result>
					<li>${result.description}
						<#if (result.hints?size > 0)>
							<ul>
								<#list result.hints as hint>
									<li>${hint}</li>
								</#list>
							</ul>
						</#if>
					</li>
				</#list>
			</ul>

		</#if>

		<#if (globalResults?size > 0)>
			<h3>Highlights</h3>
			<ul>
				<#list globalResults as result>
					<li>${result.description}
						<#if (result.hints?size > 0)>
							<ul>
								<#list result.hints as hint>
									<li>${hint}</li>
								</#list>
							</ul>
						</#if>
					</li>
				</#list>
			</ul>
		</#if>

		<#if (linkResults?size > 0)>
			<h3>Links</h3>
			<ul class='links'>
				<#list linkResults as result>
					<li><a href='${result.link}'>${result.link}</a><span class='dash'>-</span>${result.description}</li>
				</#list>
			</ul>
		</#if>

		<#if (lineNumberSuggestions?size > 0)>
			<h3>Notification</h3>
			<ul class='notifications'>
				<#list lineNumberSuggestions as dr>
					<li class='notification ${dr.level?lower_case}'><a href="#${dr.hashCode()?c}">${dr.description}</a></li>
				</#list>
			</ul>
		</#if>
		
		</div>
		</div>
	</#if>

		<div class='windupReportBody'>

			<pre id='source'><#t><#rt>
				${sourceText?html}<#t><#rt>
			</pre><#t><#rt>

		</div>
		

		<#include "resource-script.ftl">
