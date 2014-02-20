@echo off

call Scripts\setClasspath.bat

java offensive.Server.Osrv -ConfigFilePath configuration\osrv\osrv.config %*