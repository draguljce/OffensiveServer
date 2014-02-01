@echo off

if NOT defined CLASSPATH (
	set CLASSPATH=.
	call Scripts\setClasspath.bat
)

java offensive.Server.Osrv -ConfigFilePath C:\Users\john\SkyDrive\Offensive\workspace\osrv\configuration\osrv\osrv.config %*