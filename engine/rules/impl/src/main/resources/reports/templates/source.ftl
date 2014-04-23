<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${application.applicationName} - Application Report</title>
    <link href="${report.relativeFrom}resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="${report.relativeFrom}resources/css/windup.css" rel="stylesheet" media="screen">
	<link rel='stylesheet' type='text/css' href='${report.relativeFrom}resources/libraries/snippet/jquery.snippet.min.css' />
	<link rel='stylesheet' type='text/css' href='${report.relativeFrom}resources/css/windup-source.css' />
	<link rel='stylesheet' type='text/css' href='${report.relativeFrom}resources/libraries/sausage/sausage.css' />
  </head>
  <body role="document">
    
    <!-- Fixed navbar -->
    <div class="navbar-fixed-top windup-bar" role="navigation">
      <div class="container theme-showcase" role="main">
        <img src="${report.relativeFrom}/resources/img/windup-logo.png" class="logo"/>
      </div>
    </div>


    <div class="container" role="main">
        <div class="row">
          <div class="page-header page-header-no-border">
            <h1>Source Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${source.sourceName}</small></h1>
             <div class="navbar navbar-default">
            <div class="navbar-header">
              <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </button>
            </div>
            <div class="navbar-collapse collapse navbar-responsive-collapse">
              <ul class="nav navbar-nav">
                <li><a href="../index.html">Application</a></li>
                <li><a href="../ejbs.html">EJBs</a></li>
                <li><a href="../hibernate.html">Hibernate</a></li>
                <li><a href="../spring.html">Spring</a></li>
                <li><a href="../server-resources.html">Server Resources</a></li>
                <li><a href="classloader-blacklists.html">Blacklists</a></li>
                <li><a href="classloader-duplicate.html">Duplicates</a></li>
                <li><a href="classloader-notfound.html">Not Found</a></li>
              </ul>
            </div><!-- /.nav-collapse -->
            </div>
          </div>
        </div>
    </div>



    <div class="container theme-showcase" role="main">

	<pre id='source'><#t><#rt>
		${source.sourceBody?html}<#t><#rt>
	</pre><#t><#rt>
	
    </div> <!-- /container -->

    <script src="${report.relativeFrom}/resources/js/jquery-1.7.min.js"></script>
    <script src="${report.relativeFrom}/resources/js/bootstrap.min.js"></script>
    
	<script type='text/javascript' src='${report.relativeFrom}/resources/libraries/jquery-ui/jquery.ui.widget.js'></script>
	<script type='text/javascript' src='${report.relativeFrom}/resources/libraries/snippet/jquery.snippet.min.js'></script>
	<script type='text/javascript' src='${report.relativeFrom}/resources/libraries/snippet/jquery.snippet.java-properties.js'></script>
	<script type='text/javascript' src='${report.relativeFrom}/resources/libraries/snippet/jquery.snippet.java-manifest.js'></script>
	<script type='text/javascript' src='${report.relativeFrom}/resources/libraries/sausage/jquery.sausage.min.js'></script>

    
	    
	<script type='text/javascript'>
		$(document).ready(function(){
			$('pre').snippet('${source.sourceType}',{style:'ide-eclipse', showNum:true,boxFill:'#ffeeb9', box: '${source.sourceBlock}' });
	
	
		<#list source.sourceLineAnnotations as lineAnnotation>
			<#assign lineNumber = lineAnnotation.lineNumber>
			$("<div id='${lineNumber?c}-inlines' class='inline-source-hint-group'/>").appendTo('ol.snippet-num li:nth-child(${lineNumber?c})');
		</#list>
		
		<#list source.sourceLineAnnotations as lineAnnotation>
			<#assign lineNumber = lineAnnotation.lineNumber>
			
			<#compress>
			$("<a name='${lineAnnotation.hashCode()?c}'></a><#t>
				<div class='inline-source-comment green'><#t>
					<#if lineAnnotation.title?has_content>
						<div class='inline-comment'><div class='inline-comment-heading'><strong class='notification ${lineAnnotation.level}'>${lineAnnotation.title?js_string}</strong></div><#t>
							<#if lineAnnotation.hints??>
								<#list lineAnnotation.hints as hint>
										<div class='inline-comment-body'>${hint?j_string}</div><#t>
								</#list>
							</#if>
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