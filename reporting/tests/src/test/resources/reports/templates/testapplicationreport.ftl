Application Name:
  ${applicationReport.applicationName}

Child Reports:
  <#list applicationReport.childReports.iterator() as child>
    Child Class: ${child.class.name}
    Child myproperty: ${child.myProperty}
    Child noCast.referencedFrom: ${child.referencedFrom}
  </#list>