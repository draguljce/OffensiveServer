#!/bin/bash
USERNAME=postgres
PGPASSWORD=seanpaul
if [ "$1" ]; then 
	USERNAME=$1
fi
if [ "$2" ]; then
	PGPASSWORD=$2
fi
psql -d postgres -f /home/veljkoj/osrv/Database/LoadDbs.sql -v ON_ERROR_STOP=1
if [ $? -eq 0 ]; then 
	echo Databases loaded SUCCESSFULLY!!!
else 
	echo Databases loading FAILED!!!
fi
