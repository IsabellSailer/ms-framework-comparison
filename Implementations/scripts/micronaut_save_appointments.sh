#!/bin/bash
#
# Execution: ./micronaut_save_appointment.sh <port>
#

function save_appointment {

NAME="$1"
DATE=$2
START=$3
END=$4
TREATID=$5

curl -X "POST" "http://localhost:${PORT}/appointments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "customerName": "'"${NAME}"'",
  "date": "'"${DATE}"'",
  "startTime": "'"${START}"'",
  "endTime": "'"${END}"'",
  "treatmentId": "'"${TREATID}"'"
}'  

}

export PORT=$1

save_appointment "Samantha Jones" "30.03.2020" 11 14 3
save_appointment "Carrie Bradshaw" "30.03.2020" 15 18 1 
save_appointment "Garbiella Solis" "01.04.2020" 15 16 5
save_appointment "Charlotte York" "30.03.2020" 9 10 2 
save_appointment "Miranda Hobbes" "01.04.2020" 10 13 4


echo "Appointments successfully saved!"



