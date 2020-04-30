# switch to appointments directory because models.py is here
cd appointments 

# add current directory to python path so that alembic can find the module
WORKDIR=$(pwd)
export PYTHONPATH=$WORKDIR:$PYTHONPATH

# initiate alembic
alembic stamp head
alembic revision --autogenerate -m "create appointment table" 
alembic upgrade head

cd ..
nameko run --config config-docker.yml appointments.service