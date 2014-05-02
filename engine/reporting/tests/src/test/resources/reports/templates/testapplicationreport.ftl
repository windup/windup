Application Name:
  ${applicationReport.applicationName}

Child Reports:
  <#list applicationReport.childReports.iterator() as child>
    Child Class: ${child.class.name}
    Child myproperty: ${child.myProperty}
    Child noCast.referencedFrom: ${child.referencedFrom}
    Child After Cast.referencedFrom: ${graphCast(child, "org.jboss.windup.addon.reporting.meta.ClassLoaderReportModel").referencedFrom}
  </#list>