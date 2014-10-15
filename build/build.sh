##  Determine this script's location ("Tools" home dir).
scriptPath="$(cd "${0%/*}" 2>/dev/null; echo "$PWD"/"${0##*/}")"
# For the case when called through a symlink
scriptPath=`readlink -f "$scriptPath"`
scriptDir=`dirname $scriptPath`

mvn clean install -s $scriptDir/settings.xml $@
