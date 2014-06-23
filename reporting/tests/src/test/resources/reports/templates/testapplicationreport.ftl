Application Name:
  ${applicationReport.applicationName}

Child Reports:
  <#list applicationReport.childReports.iterator() as child>
    Child Class: ${child.class.name}
    Child noCast.referencedFrom: ${child.referencedFrom}
  </#list>