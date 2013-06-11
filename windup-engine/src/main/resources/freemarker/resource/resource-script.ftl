<script type='text/javascript'>
	$(document).ready(function(){
		$('pre').snippet('${snippet}',{style:'ide-eclipse', showNum:true,boxFill:'#ffeeb9', box: '${blockSetting}' });


	<#list lineNumberSuggestions as dr>
		<#assign lineNumber = dr.lineNumber>
		$("<div id='${lineNumber?c}-inlines' class='inline-source-hint-group'><#t><#t><#rt>
		").appendTo('ol.snippet-num li:nth-child(${lineNumber?c})');
	</#list>
	
	<#list lineNumberSuggestions as dr>
		<#assign lineNumber = dr.lineNumber>
		
		$("<a name='${dr.hashCode()?c}'></a><#t>
			<div class='inline-source-comment green'><#rt><#rt>
				<#if dr.description?has_content>
					<div class='inline-comment'><div class='inline-comment-heading'><h2 class='notification ${dr.level?lower_case}'>${dr.description?js_string}</h2></div><#t><#rt>
						<#if dr.hints??>
							<#list dr.hints as hint>
									<div class='inline-comment-body'>${hint?j_string}</div><#t><#rt>
							</#list>
						</#if>
					</div><#t><#rt>
				</#if>
			</div><#t><#rt>
		").appendTo('#${lineNumber?c}-inlines');<#t><#rt>
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