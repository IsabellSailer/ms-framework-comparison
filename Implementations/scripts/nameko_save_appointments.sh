#!/bin/bash
#
# Execution: ./nameko_save_appointment.sh
#

function save_appointment {

ID=$1
NAME="$2"
DATE="$3"
START=$4
END=$5

curl -XPOST -H 'Content-Type: application/json; charset=utf-8' -d $'{"treatment_id": '${ID}', "customer_name": "'"${NAME}"'", "date": "'"${DATE}"'", "start_time": '${START}', "end_time": '${END}'}' "http://localhost:${PORT}/appointments/"

}

export PORT=8081

save_appointment 3 "Samantha Jones" "2020-03-30" 11 14
save_appointment 1 "Carrie Bradshaw" "2020-03-30" 15 18 
save_appointment 5 "Garbiella Solis" "2020-04-01" 15 16
save_appointment 2 "Charlotte York" "2020-03-30" 9 10 
save_appointment 4 "Miranda Hobbes" "2020-04-01" 10 13



echo "Appointments successfully saved!"








