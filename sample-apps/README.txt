
The jee-example-jboss and jee-example-weblogic apps came from: 

  jee-example-weblogic: git@github.com:OndraZizka/jee-migration-example-app.git 2d42122b

  jee-example-jboss:    git@github.com:OndraZizka/jee-migration-example-app.git f0e1253c

Both were derived from the WebLogic app by Brad Davis.

The WebLogic app needs a client jar, which is not distributed due to licensing.
To get that jar, follow: 
http://mohanrajk.wordpress.com/2009/03/02/weblogic-103-and-maven-integration/
https://issues.jboss.org/browse/WINDUP-46?focusedCommentId=12955885&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-12955885

For that reason, the WebLogic app module is in a profile.