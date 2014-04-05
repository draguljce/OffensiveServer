@echo off

call Scripts\setClasspath.bat

java offensive.Server.OsrvGame -ConfigFilePath configuration\osrv\osrv.config %*