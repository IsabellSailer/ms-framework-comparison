#!/bin/bash
#
# Execution: ./jolie_save_appointment.sh 
#

function save_appointment {

NAME="$1"
DATE="$2"
START=$3
END=$4
TREATID=$5
ID=$6

curl -X "POST" "http://localhost:8081/appointments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  	  "customer_name": "'"${NAME}"'",
	  "id": '${ID}',
 	  "date": "'"${DATE}"'",
	  "start_time": '${START}',
	  "end_time": '${END}',
	  "treatment_id": '${TREATID}'
}'  

}

save_appointment "Samantha Jones" "30.03.2020" 11 14 3 1
save_appointment "Carrie Bradshaw" "30.03.2020" 15 18 1 2
save_appointment "Garbiella Solis" "01.04.2020" 15 16 5 3
save_appointment "Charlotte York" "30.03.2020" 9 10 2 4
save_appointment "Miranda Hobbes" "01.04.2020" 10 13 4 5


echo "Appointments successfully saved!"





