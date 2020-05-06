#!/bin/bash
#
# Execution: ./jolie_save_treatmens.sh 
#

function save_treatment {

NAME="$1"
PRICE=$2
MINDUR=$3
MAXDUR=$4
ID=$5

curl -X "POST" "http://localhost:8080/treatments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "id": '${ID}',
  "name": "'"${NAME}"'",
  "price": '${PRICE}',
  "minduration": '${MINDUR}',
  "maxduration": '${MAXDUR}'
}' 

}


save_treatment "Lash Lifting" 25 2 3 1
save_treatment "Replenish Nails" 40 1 3 2
save_treatment "Dermablading" 75 3 5 3
save_treatment "Pedicure" 15 2 3 4
save_treatment "Pluck Eyebrows" 20 1 1 5

echo "Treatments successfully saved!"



