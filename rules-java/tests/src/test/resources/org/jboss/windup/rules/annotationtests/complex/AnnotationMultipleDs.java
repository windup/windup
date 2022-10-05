package org.jboss.windup.rules.annotationtests.complex;

import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@DataSourceDefinitions({
        @DataSourceDefinition(
                name = "jdbc/multiple-ds-xa",
                className = "com.example.MyDataSource",
                portNumber = 6689,
                serverName = "example.com",
                user = "lance",
                password = "secret"
        ),
        @DataSourceDefinition(
                name = "jdbc/multiple-ds-non-xa",
                className = "com.example.MyDataSource",
                portNumber = 6689,
                serverName = "example.com",
                user = "lance",
                password = "secret",
                transactional = false
        ),
        @DataSourceDefinition(
                name = "jdbc/has-some-nulls",
                className = "com.example.HasSOmeNulls",
                portNumber = 6689,
                serverName = null,
                user = "otheruser",
                password = "othersecret",
                transactional = true
        ),
})
public class AnnotationMultipleDs {
}
