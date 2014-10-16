##  Determine this script's location ("Tools" home dir).
scriptPath="$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"
# For the case when called through a symlink
scriptPath=`readlink -f "$scriptPath"`
scriptDir=`dirname $scriptPath`

SETTINGS="-s $scriptDir/settings.xml"
GAtoCheck="org.jboss.windup:ui"

localRepo=""
for val in "$@"
do
    if [[ "$val" == -Dmaven.repo.local=* ]] ; then localRepo=$val; fi
done

## Priming build to avoid WINDUP-322
if ! mvn $SETTINGS $localRepo dependency:get -o -Dartifact=$GAtoCheck:\${project.version} > /dev/null
then
    echo -e "\n\nRunning a priming build...\n\n"
    mvn $SETTINGS $localRepo install -DskipTests
    echo -e "\n\nPriming build finished.\n\n"
fi


set -x
mvn $SETTINGS clean install $@
