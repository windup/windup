<#--
    getMigrationEffortPointsForProject(
        traversal : ProjectModelTraversal - can be created by getProjectTraversal(ProjectModel project, String mode)
        recursive : boolean
        spMode : String - "UNIQUE" | "SHARED" | "MIXED"
        includeTags : Set<String> (optional)
        excludeTags : Set<String> (optional)
    )

    Traverses through the project tree using given traversal and sums all effort (story) points.
-->
<#assign SP_UNIQUE = "UNIQUE" >
<#assign SP_SHARED = "SHARED" >
<#assign SP_MIXED  = "MIXED" >

<#function getMigrationEffortPointsForProject traversal recursive spMode includeAndExcludeTagParameters...>
    <#if includeAndExcludeTagParameters?size == 0>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, spMode)>
    <#elseif includeAndExcludeTagParameters?size == 1>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, spMode, includeAndExcludeTagParameters[0])>
    <#elseif includeAndExcludeTagParameters?size gt 1>
        <#assign effortLevels = getEffortDetailsForProjectTraversal(traversal, recursive, spMode, includeAndExcludeTagParameters[0], includeAndExcludeTagParameters[1])>
    </#if>

    <#assign totalEffort = 0>
    <#list effortLevels?keys as effortLevel>
        <#assign totalEffort = totalEffort + (effortLevel * effortLevels?api.get(effortLevel)) >
    </#list>
    <#return totalEffort>
</#function>