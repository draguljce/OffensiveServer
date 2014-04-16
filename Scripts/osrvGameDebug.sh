. ./Scripts/setClasspath.sh

java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8001 offensive.Server.OsrvGame -ConfigFilePath ./configuration/osrv/osrv.config $@
