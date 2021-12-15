<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>Source Report for ${reportModel.reportName?html}</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen"/>
    <link rel="stylesheet" type="text/css" href="resources/libraries/snippet/jquery.snippet.min.css" />
    <link rel="stylesheet" type="text/css" href="resources/css/windup-source.css" />
    <link rel="stylesheet" type="text/css" href="resources/libraries/sausage/sausage.css" />
    <link rel="shortcut icon" href="resources/img/mta-icon.png" type="image/x-icon"/>

    <script src="resources/js/jquery-3.3.1.min.js"></script>
</head>
<body role="document" class="source-report">

    <div class="navbar navbar-inverse navbar-fixed-top" id="main-navbar" style="display: none">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
        </div>

        <#include "include/navbar_macro.ftl">

        <#list reportModel.projectEdges as toProjectEdge>
            <#assign applicationIndex = projectModelToApplicationIndex(toProjectEdge.projectModel)/>
            <#if applicationIndex??>
                <div class="navbar-collapse collapse navbar-responsive-collapse project-specific" data-project-id="${toProjectEdge.projectModel.getElement().id()?c}">
                    <@renderNavbar applicationIndex/>
                </div><!-- /.nav-collapse -->
            </#if>
        </#list>
    </div>


    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Source Report
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report displays what Migration Toolkit for Applications by Red Hat found in individual files. Each item is shown below the line it was found on, and next to it, you may find a link to the rule which it was found by."></i></div>

                    <#list reportModel.projectEdges as toProjectEdge>
                        <div class="path project-specific" data-project-id="${toProjectEdge.projectModel.getElement().id()?c}">
                            ${toProjectEdge.fullPath?html}
                        </div>
                    </#list>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

                <#if reportModel.sourceFileModel.classificationModels?has_content || getTechnologyTagsForFile(reportModel.sourceFileModel)?has_content>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Information</h3>
                    </div>
                    <div class="panel-body" style="overflow: auto;">

                        <!--<div style="height: 120pt; float:left;"></div> Keeps the minimal height. -->
                        <div class="points" style="text-align: center; color: #00254b; padding-bottom: 1ex;">
                            <div class="number">${getMigrationEffortPointsForFile(reportModel.sourceFileModel)}</div>
                            <div>Story Points</div>
                        </div>

                        <div class="info" style="margin-left: 95pt;">

                            <#assign techTagsForFile = iterableToList(getTechnologyTagsForFile(reportModel.sourceFileModel))>
                            <#assign tagsForClassificationsAndHints = getTagsFromFileClassificationsAndHints(reportModel.sourceFileModel)>

                            <#if (techTagsForFile?size + tagsForClassificationsAndHints?size) &gt; 0>
                                <h4>Technologies</h4>
                                <div class="technologies" style="overflow: auto"><!-- "auto" to contain all the tags. -->
                                    <#list techTagsForFile as techTag>
                                        <span class="label label-${(techTag.level! == 'IMPORTANT')?then('danger','info')}" title="${techTag.level}">${techTag.name}</span>
                                    </#list>
                                    <#list tagsForClassificationsAndHints as techTag>
                                        <span class="label label-info" title="${techTag}">${techTag}</span>
                                    </#list>
                                </div>
                            </#if>

                            <#list reportModel.sourceFileModel.classificationModels>
                                <ul class="classifications">
                                    <#items as item>
                                        <#if item.classification??>
                                            <li>
                                                <div class="title">
                                                    <em>${item.classification!}</em>
                                                    <@render_rule_link renderType='glyph' ruleID=item.ruleID class='rule-link'/><#-- Link to the rule -->
                                                </div>
                                                <#if item.description??><div class="desc">${item.description}</div></#if>
                                                <@render_link model=item layout='ul'/><#-- Link contained in classification -->
                                            </li>
                                        </#if>
                                    </#items>
                                </ul>
                            </#list>

                            <#list reportModel.sourceFileModel.linksToTransformedFiles >
                            <h4>Automatically Translated Files</h4>
                            <ul>
                                <#items as link>
                                    <li><a href="${link.link}">${link.description!}</a></li>
                                </#items>
                            </ul>
                            </#list>

                            <div style="clear: both;"/><!-- Snaps under the height keeper. Yes, the same effect could be achieved by a table. -->
                        </div><!-- .info -->
                    </div>
                </div>
                </#if>



                <pre id="source">
                    ${reportModel.sourceBody?html}<#t>
                </pre><#t>

            </div> <!-- /container -->
        </div><!-- /row-->
    </div><!-- /container main-->

    <#include "include/timestamp.ftl">


    <script src="resources/js/jquery-migrate-1.4.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>

    <script type="text/javascript" src="resources/libraries/jquery-ui/jquery.ui.widget.js"></script>
    <script type="text/javascript" src="resources/libraries/snippet/jquery.snippet.min.js"></script>
    <script type="text/javascript" src="resources/libraries/snippet/jquery.snippet.java-properties.js"></script>
    <script type="text/javascript" src="resources/libraries/snippet/jquery.snippet.java-manifest.js"></script>
    <script type="text/javascript" src="resources/libraries/sausage/jquery.sausage.min.js"></script>

    <@renderNavbarJavaScript />

    <script type="text/javascript">
        $(window).on("hashchange", function () {
            window.scrollTo(window.scrollX, window.scrollY - 50);
        });
        function offsetAnchor() {
            if(location.hash.length !== 0) {
                window.scrollTo(window.scrollX, window.scrollY - 50);
            }
        }
        window.setTimeout(function() {
            offsetAnchor();
        }, 1);
        $(document).ready(function(){
            $("pre").snippet("${reportModel.sourceType}",{style:"ide-eclipse", showNum:true,boxFill:"#ffeeb9", box: "${reportModel.sourceBlock}" });

        <#list reportModel.sourceFileModel.inlineHints as hintLine>
            <#assign lineNumber = hintLine.lineNumber>
            $("<div id='${lineNumber?c}-inlines' class='inline-source-hint-group'/>").appendTo('ol.snippet-num li:nth-child(${lineNumber?c})');
        </#list>

        <#list reportModel.sourceFileModel.inlineHints as hintLine >
            <#assign lineNumber = hintLine.lineNumber>
            <#assign hintClasses = hintLine.tags?join(" tag-","none")>

            $("<a name='${hintLine.getElement().id()?c}' class='windup-file-location'></a><#t>
                <div class='inline-source-comment green tag-${hintClasses}'><#t>
                    <#if hintLine.hint?has_content>
                        <div class='inline-comment'><#t>
                            <div class='inline-comment-heading'><#t>
                                <strong class='notification ${effortPointsToCssClass(hintLine.effort)}'><#t>
                                    ${hintLine.title?js_string}<#t>
                                </strong><#t>
                                <@render_rule_link renderType='glyph' ruleID=hintLine.ruleID class='rule-link floatRight'/><#t>
                                <#t>
                            </div><#t>
                            <div class='inline-comment-body'><#t>
                                ${markdownToHtml(hintLine.hint)?js_string}<#t>
                                <#if hintLine.links?? && hintLine.links?has_content>
                                        <ul><#t>
                                            <#list hintLine.links as link>
                                                <li><#t>
                                                    <a href='${link.link}' target='_blank'>${link.description}</a><#t>
                                                </li><#t>
                                            </#list>
                                        </ul><#t>
                                </#if>
                            </div><#t>
                        </div><#t>
                    </#if>
                </div><#t>
            ").appendTo('#${lineNumber?c}-inlines');<#t>

        </#list>

            if (location.hash) {
                var atag = $("a[name='" + location.hash.substr(1)  +  "']");
                $('html,body').animate({scrollTop: atag.offset().top - 150},'slow');
            }

            $('code[class]').each(function(){
                 var codeSyntax = ($(this).attr('class'));
                 if(codeSyntax) {
                    $(this).parent().snippet(codeSyntax,{style:'ide-eclipse', menu:false, showNum:false});
                 }
            });
            $(window).sausage({ page: 'li.box' });
            $(window).resize(function () {
                $('div.sausage-set').css('top', parseInt($('#main-navbar').css("height")));
            });

            // Deprecated
            // $(window).load(function () {
            //     $('div.sausage-set').css('top', parseInt($('#main-navbar').css("height")));
            // });
            $(window).on('load', function () {
                $('div.sausage-set').css('top', parseInt($('#main-navbar').css("height")));
            });
        });

        function qs(key) {
            key = key.replace(/[*+?^$.\[\]{}()|\\\/]/g, "\\$&"); // escape RegEx meta chars
            var match = location.search.match(new RegExp("[?&]"+key+"=([^&]+)(&|$)"));
            return match && decodeURIComponent(match[1].replace(/\+/g, " "));
        }

        $(document).ready(function() {
            <#-- Leave a marker to indicate which project id is canonical -->
            var defaultProjectID = ${reportModel.sourceFileModel.projectModel.rootProjectModel.getElement().id()?c};
            var selectedProject = qs("project");
            if (!selectedProject)
                selectedProject = defaultProjectID;

            $(".project-specific").each(function(index, element) {
                var currentProject = $(element).data("project-id");

                if (currentProject == selectedProject)
                    $(element).show();
                else
                    $(element).remove();
            });
            $("#main-navbar").show();
        });
    </script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
