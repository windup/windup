@ECHO OFF

SET "WINDUP_LOCATION=C:\windup\windup-cli-0.6.8"
SET "DECOMPILER_LOCATION=C:\windup\jad"

IF "%1"=="" GOTO :USAGE
IF "%1"=="/?" GOTO :USAGE

::SET VARIABLES BASED ON PARAMETERS::
::-input
::-javaPkgs
::<-output>
::<any extra parameters>
SET input=
SET javaPkgs=
SET output=
SET extras=

:START_INPUT
IF "%1"=="" GOTO :END_INPUT

IF "%1"=="-i" (
  SET "input=%2"
  SHIFT
  SHIFT
  GOTO :START_INPUT
)

IF "%1"=="-input" (
  SET "input=%2"
  SHIFT
  SHIFT
  GOTO :START_INPUT
)

IF "%1"=="-p" (
  SET "javaPkgs=%2"
  SHIFT
  SHIFT
  GOTO :START_INPUT
)

IF "%1"=="-javaPkgs" (
  SET "javaPkgs=%2"
  SHIFT
  SHIFT
  GOTO :START_INPUT
)

IF "%1"=="-o" (
  SET "output=%2"
  SHIFT
  SHIFT
  GOTO :START_INPUT
)

IF "%1"=="-output" (
  SET "output=%2"
  SHIFT
  SHIFT
  GOTO :START_INPUT
)

::for any extra optional parameters
SET "extras=%extras% %1 %2"
SHIFT
SHIFT
GOTO :START_INPUT

:END_INPUT

::PROMPT FOR REQUIRED MISSING VALUES
IF "x%input%"=="x" (
  SET /P input=Enter full path of source/archive to report on: 
)

IF "x%javaPkgs%"=="x" (
  SET /P javaPkgs=Enter the Java packages to report on: 
)

::VALIDATION OF PARAMETERS::
::check to make sure input exists
IF NOT EXIST %input% (
  GOTO :MISSING_SOURCE
)

::set the new path here
SET "OLDPATH=%PATH%"
SET "PATH=%PATH%;%WINDUP_LOCATION%"

::if the %input% is not a directory, it is an archive and we will need to decompile
SET source=
IF EXIST "%input%\" (
  SET "source=true" 

  IF "x%output%"=="x" (
    ECHO NOTE: Your output directory was not provided and will default to %input%--doc
  )
) ELSE (
  SET "source=false"
  SET "PATH=%PATH%;%DECOMPILER_LOCATION%"
   
  IF "x%output%"=="x" (
    SET "extension=%input:~-3,3%"
    ECHO NOTE: Your output directory was not provided and will default to %input:~0,-4%-%extension%-doc
  )
)

::check if %output% is provided before deciding on which command to run
IF "x%output%"=="x" (
  java -jar windup-cli.jar -input %input% -javaPkgs %javaPkgs% -source %source% %extras%
) ELSE (
  java -jar windup-cli.jar -input %input% -javaPkgs %javaPkgs% -output %output% -source %source% %extras%
)

::RESET PATH
SET "PATH=%OLDPATH%"
GOTO :END

:MISSING_SOURCE
ECHO Source or Archive to report on '%input%' does not exist
GOTO :END

:USAGE
ECHO "Usage: jboss-windup.bat -i|input <input> -p|javaPkgs <java packages> [-o|output <output directory>]"

:END