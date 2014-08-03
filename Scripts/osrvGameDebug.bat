call Scripts\setClasspath.bat

java -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:8000,server=y,suspend=n offensive.Server.OsrvGame -ConfigFilePath configuration\osrv\osrv.config %*