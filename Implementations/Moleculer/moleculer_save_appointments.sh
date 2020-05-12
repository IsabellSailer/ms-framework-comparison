#!/bin/bash
#
# Execution: ./micro_save_appointment.sh <port>
#

function save_appointment {

NAME="$1"
DATE=$2
START=$3
END=$4
TREATID=$5
ID=$6

curl -X "POST" "http://localhost:${PORT}/api/appointments" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "customerName": "'"${NAME}"'",
  "date": "'"${DATE}"'",
  "startTime": '${START}',
  "endTime": '${END}',
  "treatmentId": '${TREATID}',
  "id": '${ID}'
}'  \
-v

}

export PORT=$1

save_appointment "Samantha Jones" "31.03.2020" 11 14 3 1
save_appointment "Carrie Bradshaw" "31.03.2020" 15 18 1 2
save_appointment "Garbiella Solis" "02.04.2020" 15 16 5 3
save_appointment "Charlotte York" "31.03.2020" 9 10 2 4
save_appointment "Miranda Hobbes" "02.04.2020" 10 13 4 5

echo "Appointments successfully saved!"



