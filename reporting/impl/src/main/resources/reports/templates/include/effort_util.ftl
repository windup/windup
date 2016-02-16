<#-- getMigrationEffortPointsForProject(projectModel, recursive, includeTags (optional), excludeTags (optional)) -->
<#function getMigrationEffortPointsForProject projectModel recursive includeAndExcludeTagParameters...>
    <#if includeAndExcludeTagParameters?size == 0>
        <#assign effortLevels = getEffortDetailsForProject(projectModel, recursive)>
    <#elseif includeAndExcludeTagParameters?size == 1>
        <#assign effortLevels = getEffortDetailsForProject(projectModel, recursive, includeAndExcludeTagParameters[0])>
    <#elseif includeAndExcludeTagParameters?size gt 1>
        <#assign effortLevels = getEffortDetailsForProject(projectModel, recursive, includeAndExcludeTagParameters[0], includeAndExcludeTagParameters[1])>
    </#if>

    <#assign effortLevels = getEffortDetailsForProject(projectModel, recursive, includeTags, excludeTags)>
    <#assign totalEffort = 0>
    <#list effortLevels?keys as effortLevel>
        <#assign totalEffort = totalEffort + (effortLevel * effortLevels?api.get(effortLevel)) >
    </#list>
    <#return totalEffort>
</#function>