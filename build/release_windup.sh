#!/bin/sh
 : ${1:?"Must specify release version. Ex: 2.0.1.Final"}
 : ${2:?"Must specify next development version. Ex: 2.0.2-SNAPSHOT"}

if [ -f "$HOME/.windup_profile" ]
then
   . $HOME/.windup_profile
fi

REL=$1
DEV=$2

function release_windup {
        REPO=$1
        REPODIR=$2

        cd $REPODIR
        echo Releasing \"$REPO\" - $REL \(Next dev version is $DEV\)
        mvn release:prepare-with-pom clean install \
                -DdevelopmentVersion=$DEV \
                -DreleaseVersion=$REL \
                -Dtag=$REL \
                -DskipTests \
                -Darguments=-DskipTests \
                -Dmvn.test.skip=true \
                -Dfurnace.dot.skip

        mvn -DskipTests clean install

        mvn release:prepare clean install \
                -DdevelopmentVersion=$DEV \
                -DreleaseVersion=$REL \
                -Dtag=$REL \
                -DskipTests \
                -Darguments=-DskipTests \
                -Dmvn.test.skip=true \
                -Dfurnace.dot.skip

        echo "Finished preparing release"

        mvn release:perform \
                -P jboss-release,gpg-sign \
                -DdevelopmentVersion=$DEV \
                -DreleaseVersion=$REL \
                -Dtag=$REL \
                -DskipTests \
                -Darguments=-DskipTests \
                -Dmvn.test.skip=true \
                -Dfurnace.dot.skip
        cd ..
}

WORK_DIR="windup_tmp_dir"
echo "Working in temp directory $WORK_DIR"
echo "Cleaning any previous contents from $WORK_DIR"
rm -rf $WORK_DIR
mkdir $WORK_DIR
cd $WORK_DIR
git clone git@github.com:windup/windup.git
git clone git@github.com:windup/windup-rulesets.git
git clone git@github.com:windup/windup-distribution.git

cd windup-rulesets
# this doesn't work properly as we won't have staging repository closed and used for this update. The consequense is that version is not resolvable from remote repository and will beupdated to the latest released version - effectively to previous version.
#mvn versions:update-property -DgenerateBackupPoms=false -Dproperty=version.windup -DnewVersion=$REL
sed -i -e "s/<version.windup>.*<\/version.windup>/<version.windup>$REL<\/version.windup>/g" pom.xml
git add -A
git commit -a -m "Preparing for release"
git push origin
cd ../

cd windup-distribution
# this doesn't work properly as we won't have staging repository closed and used for this update. The consequense is that version is not resolvable from remote repository and will beupdated to the latest released version - effectively to previous version.
#mvn versions:update-property -DgenerateBackupPoms=false -Dproperty=version.windup -DnewVersion=$REL
sed -i -e "s/<version.windup>.*<\/version.windup>/<version.windup>$REL<\/version.windup>/g" pom.xml
git add -A
git commit -a -m "Preparing for release"
git push origin
cd ../

release_windup git@github.com:windup/windup.git windup
release_windup git@github.com:windup/windup-rulesets.git windup-rulesets
release_windup git@github.com:windup/windup-distribution.git windup-distribution

cd windup-rulesets
# this doesn't work properly as we won't have staging repository closed and used for this update. The consequense is that version is not resolvable from remote repository and will beupdated to the latest released version - effectively to previous version.
#mvn versions:update-property -DgenerateBackupPoms=false -Dproperty=version.windup -DnewVersion=$DEV
sed -i -e "s/<version.windup>.*<\/version.windup>/<version.windup>$DEV<\/version.windup>/g" pom.xml
git add -A
git commit -a -m "Back to development"
git push origin
cd ../

cd windup-distribution
# this doesn't work properly as we won't have staging repository closed and used for this update. The consequense is that version is not resolvable from remote repository and will beupdated to the latest released version - effectively to previous version.
#mvn versions:update-property -DgenerateBackupPoms=false -Dproperty=version.windup -DnewVersion=$DEV
sed -i -e "s/<version.windup>.*<\/version.windup>/<version.windup>$DEV<\/version.windup>/g" pom.xml
git add -A
git commit -a -m "Back to development"
git push origin
cd ../

#open https://repository.jboss.org/nexus/index.html
#echo "Cleaning up temp directory $WORK_DIR"
echo "Done"
#rm -rf $WORK_DIR
