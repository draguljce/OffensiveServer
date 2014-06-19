@echo off

REM	Script arguments:
REM		-Facebook id
REM			REQUIRED
REM		-Psql path.
REM			OPTIONAL: if not provided script will assume that psql.exe is in environment variables.
REM		-Postgre user that should be used for executing scripts.
REM			OPTIONAL: default is "postgres"
REM		-Postgre user password
REM			OPTIONAL: default is "optimist"

REM	Constant for printing new line:
set println=echo.
set PSQLPATH=psql.exe
set USERNAME=postgres
set PGPASSWORD=optimist
set ROOTDIR=.\

if [%1]==[] (
	echo Facebook id must be provided!!!
)

if NOT [%2]==[] (
	set PSQLPATH=%2
)

if NOT "%3"=="" (
	set USERNAME=%3
)

if NOT "%4"=="" (
	set PGPASSWORD=%4
)

%println%
echo %PSQLPATH% -U %USERNAME% -f LoadDbs.sql -v ON_ERROR_STOP=1
%PSQLPATH% -d offensive -U %USERNAME% -c "INSERT INTO USERS VALUES (default, 'FacebookUser'); INSERT INTO FACEBOOKUSERS VALUES (%1, lastval());" -v ON_ERROR_STOP=1

%println%
if %ERRORLEVEL%==0 (
	echo User added SUCCESSFULLY!!!
) else (
	echo User adding FAILED!!!
)