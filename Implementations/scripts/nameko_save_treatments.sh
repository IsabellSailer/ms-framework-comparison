#!/bin/bash
#
# Execution: ./nameko_save_treatmens.sh
#

function save_treatment {

ID=$1
NAME="$2"
PRICE=$3
MINDUR=$4
MAXDUR=$5

curl -X "POST" "http://localhost:${PORT}/treatments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -H "Authorization: Basic YWRtaW46YWRtaW4=" \
     -d $'{
  "id": "'"${ID}"'",
  "name": "'"${NAME}"'",
  "price": "'"${PRICE}"'",
  "minduration": "'"${MINDUR}"'",
  "maxduration": "'"${MAXDUR}"'"
}' 

}

export PORT=8080

save_treatment 1 "Lash Lifting" 25 2 3 
save_treatment 2 "Replenish Nails" 40 1 3
save_treatment 3 "Dermablading" 75 3 5
save_treatment 4 "Pedicure" 15 2 4
save_treatment 5 "Pluck Eyebrows" 20 1 1

echo "Treatments successfully saved!"


