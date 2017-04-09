#run this if you changed only the windup/windup and not distribution nor ruleset. Then it will load the appropriate versions of ruleset/distribution and run them on javaee example application

cd ..
#run the current branch
mvn clean install -DskipTests
#get current version
version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)'`

#build rulesets
cd ../windup-rulesets
git fetch --all
git rebase upstream/master
rulesetVersion=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)'`
if [$rulesetVersion != $version ]
then
    git checkout $version
fi
	
mvn clean install -DskipTests

#build distribution
cd ../windup-distribution
git fetch --all
git rebase upstream/master
distributionVersion=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)'`
if [$distributionVersion != $version ]
then
    git checkout $version
fi
mvn clean install -DskipTests

#run the builded windup
cd target
unzip windup-distribution-*.zip
cd windup-distribution-*
cd bin
./windup --input ../../../../windup/test-files/jee-example-app-1.0.0.ear --output output --overwrite
cd ../../../../windup

