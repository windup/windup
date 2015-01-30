@REM ----------------------------------------------------------------------------
@REM Copyright 2012 Red Hat, Inc. and/or its affiliates.
@REM
@REM Licensed under the Eclipse Public License version 1.0, available at
@REM http://www.eclipse.org/legal/epl-v10.html
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Windup Startup script
@REM
@REM Required Environment vars:
@REM ------------------
@REM JAVA_HOME - location of a JRE home dir
@REM
@REM Optional Environment vars
@REM ------------------
@REM WINDUP_HOME - location of Windup's installed home dir
@REM WINDUP_OPTS - parameters passed to the Java VM when running Windup
@REM MAX_MEMORY - Maximum Java Heap (example: 2048m)
@REM MAX_PERM_SIZE - Maximum Permgen size (example: 256m)
@REM RESERVED_CODE_CACHE_SIZE - Hotspot code cache size (example: 128m)
@REM ----------------------------------------------------------------------------

@echo off

set ADDON_DIR=

@REM set %USERHOME% to equivalent of $HOME
if not "%USERHOME%" == "" goto OkUserhome
set "USERHOME=%USERPROFILE%"

if not "%USERHOME%" == "" goto OkUserhome
set "USERHOME=%HOMEDRIVE%%HOMEPATH%"

:OkUserhome

@REM Execute a user defined script before this one
if exist "%USERHOME%\winduprc_pre.bat" call "%USERHOME%\winduprc_pre.bat"

set ERROR_CODE=0

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto chkJVersion

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:chkJVersion
set PATH="%JAVA_HOME%\bin";%PATH%

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
   set JAVAVER=%%g
)
for /f "delims=. tokens=1-3" %%v in ("%JAVAVER%") do (
   set JAVAVER_MINOR=%%w
)

if %JAVAVER_MINOR% geq 7 goto chkFHome

echo.
echo A Java 1.7 or higher JRE is required to run Windup. "%JAVA_HOME%\bin\java.exe" is version %JAVAVER%
echo.
goto error

:chkFHome
if not "%WINDUP_HOME%"=="" goto valFHome

if "%OS%"=="Windows_NT" SET "WINDUP_HOME=%~dp0.."
if "%OS%"=="WINNT" SET "WINDUP_HOME=%~dp0.."
if not "%WINDUP_HOME%"=="" goto valFHome

echo.
echo ERROR: WINDUP_HOME not found in your environment.
echo Please set the WINDUP_HOME variable in your environment to match the
echo location of the Windup installation
echo.
goto error

:valFHome

:stripFHome
if not "_%WINDUP_HOME:~-1%"=="_\" goto checkFBat
set "WINDUP_HOME=%WINDUP_HOME:~0,-1%"
goto stripFHome

:checkFBat
if exist "%WINDUP_HOME%\bin\windup.bat" goto init

echo.
echo ERROR: WINDUP_HOME is set to an invalid directory.
echo WINDUP_HOME = "%WINDUP_HOME%"
echo Please set the WINDUP_HOME variable in your environment to match the
echo location of the Windup installation
echo.
goto error
@REM ==== END VALIDATION ====

@REM Initializing the argument line
:init
setlocal enableextensions enabledelayedexpansion
set WINDUP_CMD_LINE_ARGS=
set WINDUP_DEBUG_ARGS=

if "%1"=="" goto initArgs

set "args=%*"
set "args=%args:,=:comma:%"
set "args=%args:;=:semicolon:%"

for %%x in (%args%) do (
    set "arg=%%~x"
    set "arg=!arg::comma:=,!"
    set "arg=!arg::semicolon:=;!"
    if "!arg!"=="--debug" set WINDUP_DEBUG_ARGS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
    set "WINDUP_CMD_LINE_ARGS=!WINDUP_CMD_LINE_ARGS! "!arg!""
)

:initArgs
setlocal enableextensions enabledelayedexpansion
if %1a==a goto endInit

shift
goto initArgs
@REM Reaching here means variables are defined and arguments have been captured
:endInit

SET WINDUP_JAVA_EXE="%JAVA_HOME%\bin\java.exe"

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTCWJars

goto runWindup

@REM Start Windup
:runWindup

if exist "%WINDUP_HOME%\addons" set ADDONS_DIR=--immutableAddonDir "%WINDUP_HOME%\addons"
set WINDUP_MAIN_CLASS=org.jboss.windup.bootstrap.Bootstrap

@REM MAX_MEMORY - Maximum Java Heap (example: 2048m)
@REM MAX_PERM_SIZE - Maximum Permgen size (example: 256m)
@REM RESERVED_CODE_CACHE_SIZE - Hotspot code cache size (example: 128m)
if "%MAX_PERM_SIZE%" == "" (
  set WINDUP_MAX_PERM_SIZE=256m
) else (
  set WINDUP_MAX_PERM_SIZE=%MAX_PERM_SIZE%
)

if "%RESERVED_CODE_CACHE_SIZE%" == "" (
  set WINDUP_RESERVED_CODE_CACHE_SIZE=128m
) else (
  set WINDUP_RESERVED_CODE_CACHE_SIZE=%RESERVED_CODE_CACHE_SIZE%
)

if "%WINDUP_OPTS%" == "" (
  if "%MAX_MEMORY%" == "" (
    set WINDUP_OPTS_INTERNAL=-XX:MaxPermSize=%WINDUP_MAX_PERM_SIZE% -XX:ReservedCodeCacheSize=128m
  ) else (
    set WINDUP_OPTS_INTERNAL=-Xmx%MAX_MEMORY% -XX:MaxPermSize=%WINDUP_MAX_PERM_SIZE% -XX:ReservedCodeCacheSize=128m
  )
) else (
  set WINDUP_OPTS_INTERNAL=%WINDUP_OPTS%
)

%WINDUP_JAVA_EXE% %WINDUP_DEBUG_ARGS% %WINDUP_OPTS_INTERNAL% "-Dforge.standalone=true" "-Dforge.home=%WINDUP_HOME%" "-Dwindup.home=%WINDUP_HOME%" ^
   -cp ".;%WINDUP_HOME%\lib\*" %WINDUP_MAIN_CLASS% %WINDUP_CMD_LINE_ARGS% %ADDONS_DIR%
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set WINDUP_JAVA_EXE=
set WINDUP_CMD_LINE_ARGS=
set WINDUP_OPTS_INTERNAL=
set WINDUP_MAX_PERM_SIZE=
set WINDUP_RESERVED_CODE_CACHE_SIZE=
goto postExec

:endNT
@endlocal & set ERROR_CODE=%ERROR_CODE%

:postExec
if exist "%USERHOME%\winduprc_post.bat" call "%USERHOME%\winduprc_post.bat"

if "%WINDUP_TERMINATE_CMD%" == "on" exit %ERROR_CODE%

cmd /C exit /B %ERROR_CODE%
