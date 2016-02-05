<#function getMigrationEffortPointsForProject projectModel recursive includeTags excludeTags>
    <#assign effortLevels = getEffortDetailsForProject(projectModel, recursive, includeTags, excludeTags)>
    <#assign totalEffort = 0>
    <#list effortLevels?keys as effortLevel>
        <#assign totalEffort = totalEffort + (effortLevel * effortLevels?api.get(effortLevel)) >
    </#list>
    <#return totalEffort>
</#function>