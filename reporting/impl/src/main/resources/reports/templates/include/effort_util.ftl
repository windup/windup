<#--
    getMigrationEffortPointsForProject(
        traversal : ProjectModelTraversal - can be created by getProjectTraversal(ProjectModel project, String mode)
        recursive : boolean
        includeTags : Set<String> (optional)
        excludeTags : Set<String> (optional)
    )

    Traverses through the project tree using given traversal and sums all effort (story) points.
-->
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