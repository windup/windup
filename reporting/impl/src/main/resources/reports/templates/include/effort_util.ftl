<#--
    getMigrationEffortPointsForProject(
        traversal : ProjectModelTraversal - can be created by getProjectTraversal(ProjectModel project, String mode)
        recursive : boolean
        includeTags : Set<String> (optional)
        excludeTags : Set<String> (optional)
    )

    Traverses through the project tree using given traversal and sums all effort (story) points.
-->
<#function getMigrationEffortPointsForProject traversal recursive includeExcludeTagAndIssueCategoryParameters...>
    <#if includeExcludeTagAndIssueCategoryParameters?size == 0>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive)>
    <#elseif includeExcludeTagAndIssueCategoryParameters?size == 1>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, includeExcludeTagAndIssueCategoryParameters[0])>
    <#elseif includeExcludeTagAndIssueCategoryParameters?size == 2>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, includeExcludeTagAndIssueCategoryParameters[0], includeExcludeTagAndIssueCategoryParameters[1])>
    <#elseif includeExcludeTagAndIssueCategoryParameters?size gt 2>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, includeExcludeTagAndIssueCategoryParameters[0], includeExcludeTagAndIssueCategoryParameters[1], includeExcludeTagAndIssueCategoryParameters[2])>
    </#if>

    <#assign totalEffort = 0>
    <#list effortLevels?keys as effortLevel>
        <#assign totalEffort = totalEffort + (effortLevel * effortLevels?api.get(effortLevel)) >
    </#list>
    <#return totalEffort>
</#function>
