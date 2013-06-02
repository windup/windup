#windup-maven-plugin

Maven Plugin for JBoss Windup Tool


##Configuration Options

The following options are available for fine tuning the configuration of the plugin

* packages [List] (Required) - List of packages to target for inspection
* excludePackages [List] - List of packages to exclude from inspection
* input [File or Directory] - Base directory for which Windup will traverse (Defaults to Maven _basedir_)
* output [Directory[ - Location to store the generated reports (Defaults to _target/site/windup/_)
* source [Boolean] - Whether to run on source classes (Defaults to true)
* captureLog [Boolean] - Persist the log to a file
* logLevel [Enum] - INFO, DEBUG, WARN, ERROR, FATAL
* fetchRemote [Boolean] - fetch remote POM's for unknown jars

###Usage

Configure in an individual project POM or in a parent POM to run against a multi-module project 

The following example demonstrates basic usage.

    <build>
        <plugins>
            <plugin>
                <groupId>org.jboss.windup</groupId>
                <artifactId>windup-maven-plugin</artifactId>
                <configuration>
                    <packages>
                        <package>org.jboss.windup</package>
                    </packages>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
The following example demonstrates an advanced configuration

    <build>
        <plugins>
            <plugin>
                <groupId>org.jboss.windup</groupId>
                <artifactId>windup-maven-plugin</artifactId>
                <configuration>
                    <packages>
                        <package>org.jboss.windup</package>
                    </packages>
					<logLevel>TRACE</logLevel>
					<fetchRemote>true</fetchRemote>
					<captureLog>true</captureLog>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
###Running

To run, execute the following command

    mvn clean windup:windup