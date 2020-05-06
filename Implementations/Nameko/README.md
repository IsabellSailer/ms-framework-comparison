# Beauty Salon - Nameko

The services can be run either completely in Docker or with the services locally and only the supporting services in Docker:

* [Docker only](#Docker-only)
* [Services locally](#Services-locally)

To test the services with sample requests, you can use the scripts at [scripts](https://github.com/IsabellSailer/ms-framework-comparison/tree/master/Implementations/scripts). Further information can be found here:

* [How to use the Application ](#How-to-use-the-application)

## Docker only

### docker-compose

The easiest way to run the system is via `docker-compose`:

```bash
user:~/beautysalon$ docker-compose up
```

This will build the images and start the containers. Please note that it might take a little while until all services are up and running and database connections are established.

**Note** With the current implementation and the usage of `alembic`, the containers for the appointments service and the appointments database need to be removed after execution, otherwise the service will not be able to connect to the database correctly.

### Containers individually

Additionally, you can also build and run the containers individually. First, you have to start the supporting services:

```bash
$ docker run -d --name graphite --restart=always -p 80:80 -p 2003-2004:2003-2004 -p 2023-2024:2023-2024 -p 8125:8125/udp -p 8126:8126 graphiteapp/graphite-statsd:1.1.5-13
$ docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.7.11-management
$ docker run -d --name treatments-db -p 6379:6379 redis:5
$ docker run --name appointments-db -d -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments postgres:12-alpine
```

Second, you can build and run the services:

#### Treatments service

```bash
user:~/beautysalon/treatments$ docker build -t beautysalon-nameko/treatments:1.0 .
user:~/beautysalon/treatments$ docker run --name treatments --link treatments-db -p 8080:8080 beautysalon-nameko/treatments:1.0
```

#### Appointments service

```bash
user:~/beautysalon/appointments$ docker build -t beautysalon-nameko/appointments:1.0 .
user:~/beautysalon/appointments$ docker run --name appointments --link appointments-db --link treatments --link rabbitmq beautysalon-nameko/appointments:1.0
```

#### Confirmation service

```bash
user:~/beautysalon/confirmation$ docker build -t beautysalon-nameko/confirmation:1.0 .
user:~/beautysalon/confirmation$ docker run --name confirmation --link rabbitmq beautysalon-nameko/confirmation:1.0
```

## Services locally

To run the services locally, you can use the following instructions to set up your local development environment:

### Environment

The Application was developed on Ubuntu 18.04, so this guide provides commands for Debian based OS. 
Any other Unix-like OS would work as well (commands may be different).

### Requirements

Make sure you are logged in as an user with sudo privileges for installation.

#### Python 3.6

Ubuntu 18.04 already comes with Python 3.6 as default. If you are using another distribution you have to install it manually.
```bash
$ sudo apt-get install python3.6
```

#### venv

```bash
$ sudo apt-get install python3-venv
```

#### pip

```bash
$ sudo apt install python3-pip
```

#### Create Virtual Environments

For each of the Microservices you need different additonal Python modules, like Nameko. Each of them will only be installed in the virtual environment for the specific microservice, so that there are no dependencies between the different microservices. Therefore start with creating a virtual environment for the 3 microservices.  
Go to /beautysalon and run the commands:

```bash
user:~/beautysalon$ python3 -m venv treatments/
user:~/beautysalon$ python3 -m venv appointments/
user:~/beautysalon$ python3 -m venv confirmation/
```

Aftwerwards put the provided files of this project in the corresponding folder.

#### Nameko 3.0.0-rc8
If you want the current realase of nameko, you can simply install it with pip.
Since for this implementation the pre-release nameko v3.0.0-rc8 was used, you have to download or clone the source code from https://github.com/nameko/nameko/releases/tag/v3.0.0-rc8 and unpack it. 
In the following it is assumed that you stored it to ~/nameko-v3.0.0-rc8 .

Afterwards you have to activate the virtual environment and then install Nameko. So for example for the three microservices you run:

```bash
user:~/beautysalon$ source treatments/bin/activate
(treatments) user:~/beautysalon$ cd ~/nameko-v3.0.0-rc8
(treatments) user:~/nameko-v3.0.0-rc8$ python setup.py install
```

```bash
user:~/beautysalon$ source appointments/bin/activate
(appointments) user:~/beautysalon$ cd ~/nameko-v3.0.0-rc8
(appointments) user:~/nameko-v3.0.0-rc8$ python setup.py install
```

```bash
user:~/beautysalon$ source confirmation/bin/activate
(confirmation) user:~/beautysalon$ cd ~/nameko-v3.0.0-rc8
(confirmation) user:~/nameko-v3.0.0-rc8$ python setup.py install
```

Afterwards you have to install the following additional modules with pip.

##### Treatments

```bash
user:~/beautysalon/treatments$ source bin/activate
(teatments) user:~/beautysalon/treatments$ pip install wheel
(teatments) user:~/beautysalon/treatments$ pip install nameko-tracer
(teatments) user:~/beautysalon/treatments$ pip install nameko-statsd
(teatments) user:~/beautysalon/treatments$ pip install nameko-redis
(teatments) user:~/beautysalon/treatments$ deactivate
user:~/beautysalon/treatments$ 
```

##### Appointments

```bash
user:~/beautysalon/appointments$ source bin/activate
(appointments) user:~/beautysalon/appointments$ pip install wheel
(appointments) user:~/beautysalon/appointments$ pip install nameko-statsd
(appointments) user:~/beautysalon/appointments$ pip install nameko-tracer
(appointments) user:~/beautysalon/appointments$ pip install nameko-sqlalchemy
(appointments) user:~/beautysalon/appointments$ pip install psycopg2-binary
(appointments) user:~/beautysalon/appointments$ pip install alembic
(appointments) user:~/beautysalon/appointments$ deactivate
user:~/beautysalon/appointments$
```

##### Confirmation

```bash
user:~/beautysalon/confirmation$ source bin/activate
(confirmation) user:~/beautysalon/confirmation$ pip install nameko-tracer
(confirmation) user:~/beautysalon/confirmation$ deactivate
user:~/beautysalon/confirmation$
```

### Docker

```bash
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```

#### RabbitMQ 3.7.11 Management

```bash
$ sudo docker pull rabbitmq:3.7.11-management
```

#### PostgreSQL 12

```bash
$ sudo docker pull postgres:12-alpine
```

#### Redis 5

```bash
$ sudo docker pull redis:5
```

#### Graphiteapp/Graphite-StatsD 1.1.5-13

```bash
$ sudo docker pull graphiteapp/graphite-statsd:1.1.5-13
```

### Running the Application

#### Starting the supporting services

Before running the application, you have to start RabbitMQ, PostgreSQL, Redis and Graphiteapp/Graphite-StatsD.

```bash
$ sudo docker run --rm -d\
        -p 5672:5672\
        -p 15672:15672\
        rabbitmq:3.7.11-management
```

```bash
$ sudo docker run -d -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments -p 5432:5432 postgres:12-alpine
```

```bash
$ sudo docker run -d -p 6379:6379 redis:5
```

```bash
$ sudo docker run -d\
 --restart=always\
 -p 80:80\
 -p 2003-2004:2003-2004\
 -p 2023-2024:2023-2024\
 -p 8125:8125/udp\
 -p 8126:8126\
 graphiteapp/graphite-statsd:1.1.5-13
```

Now you can access Graphite UI by calling <http://localhost:80/dashboard> .

#### Starting the Microservices

Go to the root folder of the wanted microservice, there you have to activate the virtual environment for the corresponding microservice and then run the service.

```bash
$ source bin/activate
(venv-name) $ nameko run --config config.yml <microservice-name>.service
```

The command for running the 3 microservices of the Beauty Salon application are:

```bash
user:~/beautysalon/treatments$ source bin/activate
(teatments) user:~/beautysalon/treatments$ nameko run --config config.yml treatments.service
```

After activating the virtual environment for the Appointments microservice, you first have to create the database schema, which is done with alembic.

```bash
user:~/beautysalon/appointments$ source bin/activate
(appointments) user:~/beautysalon/appointments$ cd appointments
(appointments) user:~/beautysalon/appointments/appointments$  alembic init alembic
```

After that you have to edit the `alembic.ini` file. Change the following Parameter:

```bash
sqlalchemy.url = postgres://postgres:docker@localhost/appointments
```

In addition, also change the file `env.py` in the folder `alembic` in the following way:

Replace the line:
```
target_metadata = None
```

with the following lines:
```
from models import DeclarativeBase
target_metadata = DeclarativeBase.metadata
```

When that is done you can create the databaseschema and start the service.

```bash
(appointments) user:~/beautysalon/appointments/appointments$ alembic stamp head
(appointments) user:~/beautysalon/appointments/appointments$ alembic revision --autogenerate -m "create appointment table"
(appointments) user:~/beautysalon/appointments/appointments$ alembic upgrade head
INFO  [alembic.runtime.migration] Context impl PostgresqlImpl.
INFO  [alembic.runtime.migration] Will assume transactional DDL.
INFO  [alembic.runtime.migration] Running upgrade  -> 5a566a5fbfe3, create appointment_table
(appointments) user:~/beautysalon/appointments/appointments$ cd ..
(appointments) user:~/beautysalon/appointments$ nameko run --config config.yml appointments.service
```

```bash
user:~/beautysalon/confirmation$ source bin/activate
(confirmation) user:~/beautysalon/confirmation$  nameko run --config config.yml confirmation.service
```

#### Inital Creation of Treatments and Appointments
For the initial creation of available treatments and booked appointments, go to the /scripts folder.
Executing the nameko_save_treatments.sh script will create 5 different treatments and is called by:
```bash
user:~/scripts$ ./nameko_save_treatments.sh 
```
The the nameko_save_appointments.sh script, will create 5 appointments and is called by: 
```bash
user:~/scripts$ ./nameko_save_appointments.sh 
```

### How to use the Application 

After starting the microservices, you can communicate with the treatments and the appointments services.

#### Use Case Example

In our example use case a customer wants to book an appointment for a specific treatment. 
As for creating an appointment the ID of the desired treatment is needed, you have to get the IDs of the offered treatments first.
To view the list of all available treatments and their IDs open a new terminal and enter the command:
```bash
$ curl 'http://localhost:8080/treatments/list' 
```
It will return a list with all available treatments including all important information: ID, Name, Price, Minduration and Maxduration.

For creating an appointment you have to send a POST request to the appointments service, containing the required fields for an appointment, including the ID of the desired treatment.
```bash
$ curl -X "POST" "http://localhost:8081/appointments/" 
       -H 'Content-Type: application/json; charset=utf-8' -d $'{
	  "treatment_id": <id>, 
	  "customer_name": "<name>", 
	  "date": "<date>", 
	  "start_time": <start>, 
	  "end_time": <end>
}' 
```
Start time and end time only accept integer values between 8 and 18 and the date has to be in the format DD.MM.YYYY.
If your appointment has no conflicts with other appointments and fullfills all constraints (minduration <= duration => maxduration and start_time < end_time) 
it will be confirmed by a pop-up, created by the Confirmation service, otherwise you will receive an error message on the commandline.

#### Further Functions

Apart from the above use case, also some additional functions are offered.

##### Treatments

If you want to get the information only for one specific treatment, you have to send a GET request with the ID of the desired treatment to the treatment service.
```bash
$ curl 'http://localhost:8080/treatments/<id>' 
```

If you want to create a new treatment you have to send a POST request to the treatment service, including all required fields.
```bash
$ curl -X "POST" "http://localhost:8080/treatments/" \
       -H 'Content-Type: application/json; charset=utf-8' -d $'{
    	  "id": <id>, 
    	  "name": "<name>", 
    	  "minduration": <minduration>, 
    	  "maxduration": <maxduration>, 
    	  "price": <price>
}' 
```

##### Appointments

If you want to view all booked appointments including their information you have to send a GET request to the appointment service.
```bash
$ curl 'http://localhost:8081/appointments/list' 
```

If you only want to view the details of a specific appointment, you have to send a GET request to the appointment service, including the ID of your appointment.
```bash
$ curl 'http://localhost:8081/appointments/<id>' 
```