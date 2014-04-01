#!/bin/bash

WINDUP_LOCATION=~/windup/windup-cli-0.6.8
DECOMPILER_LOCATION=~/windup/jad
if [ $# -lt 4 ]; then
  echo "Usage: $0 -i|input <input> -p|javaPkgs <java packages> [-o|output <output directory>]"
  exit
fi

INPUT=
JAVAPKGS=
OUTPUT=
EXTRAS=

declare -a args=($@)
for (( i = 0; i < ${#args[@]}; i = i + 2 ))
do
  
  if [ ${args[$i]} = -i ] || [ ${args[$i]} = -input ]; then
    INPUT=${args[$i+1]}

  else
    if [ ${args[$i]} = -p ] || [ ${args[$i]} = -javaPkgs ]; then
      JAVAPKGS=${args[$i+1]}

    else
      if [ ${args[$i]} = -o ] || [ ${args[$i]} = -output ]; then
        OUTPUT=${args[$i+1]}

      else
        EXTRAS="$EXTRAS ${args[$i]} ${args[$i+1]}"
      fi
    fi
  fi
done

if [ -z $INPUT ]; then
  echo Enter full path of source/archive to report on: 
  read INPUT
fi

if [ -z $JAVAPKGS ]; then
  echo Enter the Java packages to report on:
  read JAVAPKGS
fi

if [ ! -e $INPUT ]; then
  echo Source or Archive to report on '$INPUT' does not exist
  exit
fi

OLDPATH=$PATH
PATH=$PATH:$WINDUP_LOCATION

SOURCE=
if [ -d $INPUT ]; then
  SOURCE=true
 
  if [ -z $OUTPUT ]; then
    echo NOTE: Your output directory was not provided and will default to $INPUT--doc
  fi
else
  SOURCE=false
  PATH=$PATH:$DECOMPILER_LOCATION

  if [ -z $OUTPUT ]; then
    LENGTH=${#INPUT}
    echo NOTE: Your output directory was not provided and will default to ${INPUT:0:($LENGTH-4)}-${INPUT:(-3)}-doc
  fi
fi

if [ -z $OUTPUT ]; then
  java -jar windup-cli.jar -input $INPUT -javaPkgs $JAVAPKGS -source $SOURCE $EXTRAS
else
  java -jar windup-cli.jar -input $INPUT -javaPkgs $JAVAPKGS -output $OUTPUT -source $SOURCE $EXTRAS
fi

PATH=$OLDPATH
