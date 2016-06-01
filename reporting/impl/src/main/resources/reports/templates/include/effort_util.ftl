<#-- getMigrationEffortPointsForProject(traversal, recursive, includeTags (optional), excludeTags (optional)) -->
<#function getMigrationEffortPointsForProject traversal recursive includeAndExcludeTagParameters...>
    <#if includeAndExcludeTagParameters?size == 0>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive)>
    <#elseif includeAndExcludeTagParameters?size == 1>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, includeAndExcludeTagParameters[0])>
    <#elseif includeAndExcludeTagParameters?size gt 1>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, includeAndExcludeTagParameters[0], includeAndExcludeTagParameters[1])>
    </#if>

    <#assign totalEffort = 0>
    <#list effortLevels?keys as effortLevel>
        <#assign totalEffort = totalEffort + (effortLevel * effortLevels?api.get(effortLevel)) >
    </#list>
    <#return totalEffort>
</#function>