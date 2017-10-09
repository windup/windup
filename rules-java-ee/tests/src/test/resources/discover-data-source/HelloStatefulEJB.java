import javax.annotation.sql.DataSourceDefinitions;
import javax.annotation.sql.DataSourceDefinition;

@DataSourceDefinitions(
        value = {
                @DataSourceDefinition(name = "java:app/env/HelloStatefulEJB_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "testdb",
                        properties = {"connectionAttributes=;create=true"}
                ),
                @DataSourceDefinition(name = "java:comp/env/HelloStatefulEJB_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.ClientXADataSource",
                        portNumber = 1527,
                        serverName = "localhost",
                        user = "APP",
                        password = "APP",
                        databaseName = "testdb",
                        properties = {"connectionAttributes=;create=true"}
                )
        }
)
public class HelloStatefulEJB {

}