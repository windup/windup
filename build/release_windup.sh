#!/bin/sh
 : ${1:?"Must specify release version. Ex: 2.0.1.Final"}
 : ${2:?"Must specify next development version. Ex: 2.0.2-SNAPSHOT"}

if [ -f "$HOME/.windup_profile" ]
then
   . $HOME/.windup_profile
fi

function release_windup {
        REL=$1
        DEV=$2
        REPO=$3
        REPODIR=$4
#        git clone $REPO $REPODIR
        cd $REPODIR
        echo Releasing \"$REPO\" - $1 \(Next dev version is $2\)
        mvn release:prepare release:perform \
                -DdevelopmentVersion=$DEV \
                -DreleaseVersion=$REL \
                -Dtag=$REL \
                -DskipTests \
                -Darguments=-DskipTests \
                -Dgwt.compiler.skip=true \
                -Dmvn.test.skip=true \
                -Dfurnace.dot.skip
        cd ..
}

WORK_DIR="windup_tmp_dir"
echo "Working in temp directory $WORK_DIR"
echo "Cleaning any previous contents from $WORK_DIR"
#rm -rf $WORK_DIR
#mkdir $WORK_DIR
cd $WORK_DIR
#git clone git@github.com:windup/windup.git
#git clone git@github.com:windup/windup-rulesets.git
#git clone git@github.com:windup/windup-distribution.git

#release_windup $1 $2 git@github.com:windup/windup.git windup
#release_windup $1 $2 git@github.com:windup/windup-rulesets.git windup-rulesets
release_windup $1 $2 git@github.com:windup/windup-distribution.git windup-distribution

#open https://repository.jboss.org/nexus/index.html
#echo "Cleaning up temp directory $WORK_DIR"
echo "Done"
#rm -rf $WORK_DIR
