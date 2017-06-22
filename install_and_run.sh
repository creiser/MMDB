#!/bin/bash

echo "${PWD}"

if [ $PWD != "/home/oracle/MMDB" ] && [ $PWD != "/u01/userhome/oracle/MMDB" ]
then
	echo "Please make sure that this script resides in /home/oracle/MMDB."
	exit 1
fi

echo "Compile indexer."
javac -cp "./lib/oracle/*" LuceneIndexer.java StoredContext.java
echo "Indexer compiled."

echo "Upload indexer to database as user HR."
loadjava -user HR/oracle StoredContext.class LuceneIndexer.class
echo "Indexer uploaded to database."

echo "Compile indexer wrapper."
javac -cp "./lib/lire/*" LuceneIndexerWrapper.java
echo "Indexer wrapper compiled."

echo "Grant permissions as SYSTEM user by running admin.sql"
echo exit | sqlplus SYSTEM/oracle @admin.sql
echo "Permissions granted."

echo "Create type, indextype and database table to store the images as HR user by running setup.sql"
echo exit | sqlplus HR/oracle @setup.sql
echo "Created indextype and image table."

echo "Upload corel10k dataset to the database and create the index."
javac -cp "./lib/oracle/*" UploadCorel.java
java -cp ".:./lib/oracle/*" UploadCorel

echo "Compile and run the front end, this will launch a webserver on port 12345"
echo "Use your browser to navigate to http://localhost:12345"
javac -cp "./lib/frontend/*:./lib/oracle/ojdbc8_g.jar" FrontEnd.java
java -cp ".:./lib/frontend/*:./lib/oracle/ojdbc8_g.jar" FrontEnd 12345






