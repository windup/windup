<!DOCTYPE html>
<html lang="en">

<#if reportModel.sourceFileModel.projectModel??>
<#assign applicationReportIndexModel = projectModelToApplicationIndex(reportModel.sourceFileModel.projectModel)>
</#if>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Source Report for ${reportModel.reportName}</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
	  <link rel='stylesheet' type='text/css' href='resources/libraries/snippet/jquery.snippet.min.css' />
	  <link rel='stylesheet' type='text/css' href='resources/css/windup-source.css' />
	  <link rel='stylesheet' type='text/css' href='resources/libraries/sausage/sausage.css' />
  </head>
  <body role="document">
	    
	<div class="navbar navbar-default navbar-fixed-top">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
		</div>
		<div class="navbar-collapse collapse navbar-responsive-collapse">
			<ul class="nav navbar-nav">
				<#include "include/navbar.ftl">
			</ul>
		</div><!-- /.nav-collapse -->
	</div>
	
	
	<div class="container-fluid" role="main">
		<div class="row">
			<div class="page-header page-header-no-border">
				<h1>Source Report<span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.sourceFileModel.prettyPath}</small></h1>
			</div>
		</div>
		
		<div class="row">
			<!-- Breadcrumbs -->
			<div class="container-fluid">
				<ol class="breadcrumb top-menu">
					<li><a href="../index.html">All Applications</a></li>
					<#include "include/breadcrumbs.ftl">
				</ol> 
			</div>
			<!-- / Breadcrumbs -->
		</div>


		<div class="row">
    		<div class="container-fluid theme-showcase" role="main">

			  <#if reportModel.sourceFileModel.classificationModels.iterator()?has_content || getTechnologyTagsForFile(reportModel.sourceFileModel).iterator()?has_content>
			    <div class="panel panel-default">
			    	<div class="panel-heading">Information</div>
			    	<div class="panel-body">
				        <ul>
				          <li>Estimated Story Points: ${getMigrationEffortPointsForFile(reportModel.sourceFileModel)}</li>
				          <#if getTechnologyTagsForFile(reportModel.sourceFileModel).iterator()?has_content>
					          <li>
					            <#list getTechnologyTagsForFile(reportModel.sourceFileModel).iterator() as techTag>
				              		<span class="label label-info">${techTag.name}</span>
				            	</#list>
				              </li>
				          </#if>
			            </ul>
			        
			            <ul>
			              <#list reportModel.sourceFileModel.classificationModels.iterator() as classificationLineItem>
				        <#if classificationLineItem.classification??>
				            <li>
								${classificationLineItem.classification!""}
								<#if classificationLineItem.description??>
									- ${classificationLineItem.description!""}
								</#if>
								<#if classificationLineItem.links??>
								<ul>
									<#list classificationLineItem.links.iterator() as link>
										<li>
											<a href='${link.link}'>${link.description}</a>
										</li>
									</#list>
								</ul>
							</li>
						</#if>
				        <#else>
				            <#list classificationLineItem.links.iterator() as link>
								<li>
									<#if link.description??>
									 <a href='${link.link}'>${link.description}</a>
									<#else>
									<a href='${link.link}'>${link.link}</a>
								       </#if>

								</li>
							</#list>	
				        </#if>
			              </#list>
			            </ul>
					</div>
			      </div>
			    </#if>  
    
    

				<pre id='source'><#t><#rt>
					${reportModel.sourceBody?html}<#t><#rt>
				</pre><#t><#rt>
	
    		</div> <!-- /container -->
    	</div><!-- /row-->
    </div><!-- /container main-->
    <script src="resources/js/jquery-1.7.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    
	<script type='text/javascript' src='resources/libraries/jquery-ui/jquery.ui.widget.js'></script>
	<script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.min.js'></script>
	<script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.java-properties.js'></script>
	<script type='text/javascript' src='resources/libraries/snippet/jquery.snippet.java-manifest.js'></script>
	<script type='text/javascript' src='resources/libraries/sausage/jquery.sausage.min.js'></script>


	    
	<script type='text/javascript'>
		$(document).ready(function(){
			$('pre').snippet('${reportModel.sourceType}',{style:'ide-eclipse', showNum:true,boxFill:'#ffeeb9', box: '${reportModel.sourceBlock}' });
	
		<#list reportModel.sourceFileModel.inlineHints.iterator() as hintLine>
			<#assign lineNumber = hintLine.lineNumber>
			$("<div id='${lineNumber?c}-inlines' class='inline-source-hint-group'/>").appendTo('ol.snippet-num li:nth-child(${lineNumber?c})');
		</#list>
		
		<#list reportModel.sourceFileModel.inlineHints.iterator() as hintLine >
			<#assign lineNumber = hintLine.lineNumber>
			
			<#compress>
			$("<a name='${hintLine.hashCode()?c}'></a><#t>
				<div class='inline-source-comment green'><#t>
					<#if hintLine.hint?has_content>
						<div class='inline-comment'><#t>
							<div class='inline-comment-heading'><#t>
								<strong class='notification ${effortPointsToCssClass(hintLine.effort)}'><#t>
									${hintLine.title?js_string}<#t>
								</strong><#t>
							</div><#t>
							<div class='inline-comment-body'><#t>
							    ${markdownToHtml(hintLine.hint)?js_string}<#t>
							    <#if hintLine.links?? && hintLine.links.iterator()?has_content>
									    <ul><#t>
    										<#list hintLine.links.iterator() as link>
											    <li><#t>
												    <a href='${link.link}'>${link.description}</a><#t>
											    </li><#t>
										    </#list>
									    </ul><#t>
							    </#if>
							</div><#t>
						</div><#t>
					</#if>
				</div><#t>
			").appendTo('#${lineNumber?c}-inlines');<#t>
			</#compress>

		</#list>


		
			$('code[data-code-syntax]').each(function(){
		         var codeSyntax = ($(this).data('code-syntax'));
		         if(codeSyntax) {
		            $(this).parent().snippet(codeSyntax,{style:'ide-eclipse', menu:false, showNum:false});
		         }
			});
			$(window).sausage({ page: 'li.box' });
		}); 
	</script>
    
  </body>
</html>
