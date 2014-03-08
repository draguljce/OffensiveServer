@echo off

REM	Script arguments:
REM		-Psql path.
REM			OPTIONAL: if not provided script will assume that psql.exe is in environment variables.
REM		-Postgre user that should be used for executing scripts.
REM			OPTIONAL: default is "postgres"
REM		-Postgre user password
REM			OPTIONAL: default is "optimist"
REM		-Directory where scripts are located
REM			OPTIONAL: default is current directory

REM	Constant for printing new line:
set println=echo.
set PSQLPATH=psql.exe
set USERNAME=postgres
set PGPASSWORD=optimist
set ROOTDIR=.\

if NOT [%1]==[] (
	set PSQLPATH=%1
)

if NOT "%2"=="" (
	set USERNAME=%2
)

if NOT "%3"=="" (
	set PGPASSWORD=%3
)

if NOT [%4]==[] (
	set ROOTDIR=%4
)

%println%
echo %PSQLPATH% -U %USERNAME% -f DropDbs.sql -v ON_ERROR_STOP=1
%PSQLPATH% -U %USERNAME% -f DropDbs.sql -v ON_ERROR_STOP=1

%println%
if %ERRORLEVEL%==0 (
	echo Databases dropped SUCCESSFULLY!!!
) else (
	echo Databases dropping FAILED!!!
)
