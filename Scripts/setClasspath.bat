@echo off
setlocal ENABLEDELAYEDEXPANSION
FOR /R .\lib %%G IN (*.jar) DO set CLASSPATH=!CLASSPATH!;%%G
Echo The Classpath definition is %CLASSPATH%