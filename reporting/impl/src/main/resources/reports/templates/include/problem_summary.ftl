<#if problemsBySeverity?has_content>
    <#list problemsBySeverity?keys as severity>
        <#list problemsBySeverity[severity] as problemSummary>
            <@write_to_disk filename="problem_summary_${problemSummary.id}.js">
                <#compress>
                    MIGRATION_ISSUES_DETAILS["${problemSummary.id}"] = [
                    <#list problemSummary.descriptions as originalDescription>
                        <#assign description = originalDescription!"-- No detailed text --">
                        <#assign ruleID = problemSummary.ruleID!"">
                        <#assign issueName = problemSummary.issueName!"">
                        {description: "${markdownToHtml(description)?js_string}", ruleID: "${ruleID?js_string}", issueName: "${issueName?js_string}",
                        problemSummaryID: "${problemSummary.id}", files: [
                        <#list problemSummary.getFilesForDescription(originalDescription) as fileSummary>
                        <#--
                            If this is an application specific report, then the report model will contain the
                             correct application. In this case the non-canonical project will be used.

                             If it is a global report, then the file model can be used to find the application associated
                             with that file. In this case, the canonical local will be used.
                        -->
                            <#assign application = reportModel.projectModel!fileSummary.file.projectModel.rootProjectModel>

                            <#assign renderedLink><@render_link model=fileSummary.file project=application/></#assign>
                            {l:"${renderedLink?json_string}", oc:"${fileSummary.occurrences?json_string}"},
                        </#list>
                        ], resourceLinks: [
                        <#list problemSummary.links! as link>
                            {h:"${link.link?json_string}", t:"${link.title?json_string}"},
                        </#list>
                        ]},
                    </#list>
                    ];

                    onProblemSummaryLoaded("${problemSummary.id}");
                </#compress>
            </@write_to_disk>
        </#list>
    </#list>
</#if>
