#!/bin/bash
set -e
: "${CONTACT_SERVICE_URL:?Set CONTACT_SERVICE_URL first, e.g. export CONTACT_SERVICE_URL=http://localhost:8081/api/contacts}"
if [ -x ./mvnw ]; then
  ./mvnw spring-boot:run
else
  mvn spring-boot:run
fi
