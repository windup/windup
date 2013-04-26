<#setting url_escaping_charset="UTF-8">

<#macro recurse_macro node>
	<#if (node.decorations?size > 0) || (node.resourceReports?size > 0)>
			 
		<#if node.vendorResult>
			<div class="archiveResult vendorResult" data-collapse id="result-${node.relativePathFromRoot?url}">
		<#else>
			<div class="archiveResult customerResult" id="result-${node.relativePathFromRoot?url}">
		</#if>  
		
			<h3 class='archiveResultTitle'>${node.relativePathFromRoot}</h3>
		
			<div>
				<div class="content">
					<@pie archive=node/>
	
					<@archivesummary archive=node/>
				
					<table class="archive-report">
					<#--list the title, header.-->
					<tr>
						<th class='archiveColumnHeaders'>Artifact</th>
						<th class='archiveColumnHeaders'>Effort</th>
						<th class='archiveColumnHeaders'>Highlights</th>
					</tr>
					
					<#list node.resourceReports as resource>
						<tr>
							<td><@modifier modification=resource.sourceModification/><a href="${resource.relativePathFromRootToReport}">${resource.title}</a></td>
							<td>${resource.effort}</td>
							<td>${resource.summary}</td>
						</tr>
					</#list>
					</table>
				</div>
			</div>
		</div>
		
	</#if>
	
	<#list node.nestedArchiveReports as nested>
		<@recurse_macro node=nested></@recurse_macro>
	</#list>
</#macro>



<div class='windupBody'>
	<div class='windupHighLevel'>
		<@overviewpie archive=archiveReport/>
	</div>
</div>
<div class='windupSummary'>
	<@recurse_macro node=archiveReport/>
</div>

<script type='text/javascript'>
	$('div.archiveResult').collapse({open: function() { this.slideDown(150); }, close: function() { this.slideUp(150); }, persist: true});
	var customerResult = new jQueryCollapse($('div.customerResult'));
	customerResult.open();
</script>
