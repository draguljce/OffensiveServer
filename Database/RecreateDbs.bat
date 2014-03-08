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

call DropDbs %1 %2 %3 %4
call CreateDbs %1 %2 %3 %4