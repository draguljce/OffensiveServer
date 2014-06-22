@echo off

REM	Script arguments:
REM		-psql path.
REM			OPTIONAL: if not provided script will assume that pd_dump.exe is in environment variables.
REM		-Postgres user that should be used for executing scripts.
REM			OPTIONAL: default is "postgres"
REM		-Postgres user password
REM			OPTIONAL: default is "optimist"
REM		-Directory where backup should be stored
REM			OPTIONAL: C:\dbBackups\offensive\offensiveBackup.txt

REM	Constant for printing new line:
set println=echo.
set PSQLPATH=psql.exe
set USERNAME=postgres
set PGPASSWORD=optimist
set TARGETLOCATION=C:\dbBackups\offensive\offensiveBackup.txt

if NOT [%1]==[] (
	set PGDUMPPATH=%1
)

if NOT "%2"=="" (
	set USERNAME=%2
)

if NOT "%3"=="" (
	set PGPASSWORD=%3
)

if NOT [%4]==[] (
	set %TARGETLOCATION%=%4
)

call DropDbs %1 %2 %3

%println%
echo %PSQLPATH% -U %USERNAME% -c "CREATE DATABASE offensive WITH ENCODING = 'UTF8';" -v ON_ERROR_STOP=1
%PSQLPATH% -U %USERNAME% -c "CREATE DATABASE offensive WITH ENCODING = 'UTF8';" -v ON_ERROR_STOP=1

%println%
echo %PSQLPATH% -U %USERNAME% -o -d offensive ^< %TARGETLOCATION%
start cmd /c %PSQLPATH% -U %USERNAME% -o -d offensive ^< %TARGETLOCATION%