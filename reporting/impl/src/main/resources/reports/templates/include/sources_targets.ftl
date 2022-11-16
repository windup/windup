<@write_to_disk filename="sources_and_targets.js">
    <#assign sources = sourcesAndTargets.getIssuesBySourceTech()/>
    <#if sources?has_content>
        let ISSUES_BY_SOURCE = {
        <#list sourcesAndTargets.getIssuesBySourceTech() as tech, issues>
            "${tech}": [
            <#list issues as issue>
                "${issue}",
            </#list>
            ],
            };
        </#list>
    </#if>

    <#assign targets = sourcesAndTargets.getIssuesByTargetTech()/>
    <#if targets?has_content>
        let ISSUES_BY_TARGET = {
        <#list sourcesAndTargets.getIssuesByTargetTech() as tech, issues>
            "${tech}": [
            <#list issues as issue>
                "${issue}",
            </#list>
            ],
            };
        </#list>
    </#if>
</@write_to_disk>