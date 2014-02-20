@echo off

call Scripts\setClasspath.bat

java offensive.Server.OsrvRegistration -ConfigFilePath configuration\osrv\osrv.config %*