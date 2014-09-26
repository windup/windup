Seam Booking Example
====================

This example demonstrates the use of Seam in a Java EE 5 environment.
Transaction and persistence context management is handled by the
EJB container.

This example runs on JBoss AS as an EAR.

 example.name=booking

To deploy this application to a cluster, first follow the steps 1-9 clustering-howto.txt in the root folder of the Seam distribution. Then execute the following command:

 ant farm

This command will deploy the archive to the farm directory of the "all" JBoss AS domain. To undeploy, run the following command:

 ant unfarm

HTTP session replication is enabled by default. You can disable it with the following commandline switch:

 -Dsession.replication=false

You can also toggle Seam's ManagedEntityInterceptor for any deployment with the following commandline switch:

 -Ddistributable=false

