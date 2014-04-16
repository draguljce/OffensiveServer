. ./Scripts/setClasspath.sh

java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n offensive.Server.OsrvRegistration -ConfigFilePath ./configuration/osrv/osrv.config
