#!/bin/sh

JAVA_OPTS="$JAVA_OPTS -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"

# Setup WINDUP_HOME
if [ "x$WINDUP_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    WINDUP_HOME=`cd $DIRNAME/..; pwd`
fi
export WINDUP_HOME

java $JAVA_OPTS -jar $WINDUP_HOME/windup-cli.jar "$@"

