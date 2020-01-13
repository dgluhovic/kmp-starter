#!/usr/bin/env bash

. backend/.env

brew install postgres
brew install postgis

createdb $DB_NAME
psql -d $DB_NAME -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD';"
psql -d $DB_NAME -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME to $DB_USER;"
psql -d $DB_NAME -c "CREATE EXTENSION POSTGIS;"

# publish to local mvn repo
# https://discuss.kotlinlang.org/t/creating-multiplatform-library-for-usage-in-other-multiplatform-projects/12860
./gradlew data:build