# Beauty Salon - Jolie

The services can be run either completely in Docker or with the services locally and only the supporting services in Docker:

* [Docker only](#Docker-only)
* [Services locally](#Services-locally)

To test the services with sample requests, you can use the scripts at [scripts](https://github.com/IsabellSailer/ms-framework-comparison/tree/master/Implementations/scripts). Further information can be found here:

* [How to get the API](#How-to-get-the-API)

## Docker only

### docker-compose

The easiest way to run the system is via `docker-compose`:

```bash
user:~/beautysalon$ docker-compose up
```

This will build the images and start the containers. Please note that it might take a little while until all services are up and running and database connections are established.

### Containers individually

Additionally, you can also build and run the containers individually. First, you have to start the supporting services:

```bash
$ docker run -d --name treatments-db -p 6379:6379 redis:5
$ docker run --name appointments-db -d -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments postgres:12-alpine
```

Second, you can build and run the services:

#### Treatments service

```bash
user:~/beautysalon/treatments$ docker build -t beautysalon-jolie/treatments:1.0 .
user:~/beautysalon/treatments$ docker run --name treatments --link treatments-db -p 8080:8080 beautysalon-jolie/treatments:1.0
```

#### Confirmation service

```bash
user:~/beautysalon/confirmation$ docker build -t beautysalon-jolie/confirmation:1.0 .
user:~/beautysalon/confirmation$ docker run --name confirmation beautysalon-jolie/confirmation:1.0
```

#### Appointments service

```bash
user:~/beautysalon/appointments$ docker build -t beautysalon-jolie/appointments:1.0 .
user:~/beautysalon/appointments$ docker run --name appointments --link appointments-db --link treatments --link confirmation beautysalon-jolie/appointments:1.0
```

## Services locally

To run the services locally, you can use the following instructions to set up your local development environment:

### Environment

The Application was developed on Ubuntu 18.04, so this guide provides commands for Debian based OS.
Any other Unix-like OS would work as well (commands may be different).

### Requirements

Make sure you are logged in as an user with sudo privileges for installation.

#### JDK Version 11

```bash
$ sudo apt install openjdk-11-jdk
```

#### Jolie 1.8.2

Download the the installer from https://www.jolie-lang.org/downloads.html .

Since the extensions jolier (for RESTful Services) is not included in this installation, you have to add it manually. 
Therefore at first clone and build the Gitlab project.
```bash
$ git clone https://github.com/jolie/jolie.git
$ sudo apt install maven
$ cd jolie
~/jolie$ mvn install
```

Go to the Repository folder and zip the /dist folder.

Now rename the jolie-1.8.2.jar folder to jolie-1.8.2.zip and open it in the exlporer. 

There you have to replace the dist.zip folder with the the zipped /dist folder form the Gitlab Project.

Now rename jolie-1.8.2.zip to jolie-1.8.2.jar again and finally install Jolie to the default location by running:

```bash
$ sudo java -jar jolie-1.8.2.jar
```

Afterwards you have to set the variable JOLIE_HOME, therefore open the file ~/.bash_profile and add the line:

```bash
$ export JOLIE_HOME=/usr/lib/jolie
```

For sourcing the file automatically edit the ~/.bashrc file and add at the end of the file:

```bash
$ source ~/.bash_profile
```

#### Docker

```bash
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```

#### PostgreSQL 12

```bash
$ sudo docker pull postgres:12-alpine
```

#### MongoDB

```bash
$ sudo docker pull mongo
```

### Running the Application

### Starting the supporting services

Before running the application, you have to start PostgreSQL and MongoDB. 
```bash
$ sudo docker run -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments -p 5432:5432 -d postgres:12-alpine
```
```bash
$ sudo docker run -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=docker -p 27017:27017 -d mongo
```

### Starting the Microservices

Go to the root folder of the desired microservice, there you can start the service with the commands:
```bash
user:~/beautysalon/treatments$ jolier treatment.ol TREATMENTS localhost:8080
```
```bash
user:~/beautysalon/appointments$ jolier appointment.ol APPOINTMENTS localhost:8081
```
```bash
user:~/beautysalon/confirmation$ jolie confirmation.ol
```

## How to get the API

You can access the API using tools enabled for processing openapi specifications, e.g. Swagger. 
Therefore you only have to import the files ~/beautysalon/treatments/TREATMENTS.json or ~/beautysalon/appointments/APPOINTMENTS.json into the tool.
If you change something in the source code of an interface of one of the microservices, you have to convert the interface into an openapi 2.0 specification again.
This is done by the command:
```bash
$ jolie2openapi <service_filename> <input_port> <router_host> <output_folder>
```

So for the Appointments microservice you run:
```bash
user:~/beautysalon/appointments$ jolie2openapi appointment.ol APPOINTMENTS localhost:8081 . 
```
For the Treatments microservice you run: 
```bash
user:~/beautysalon/appointments$ jolie2openapi treatment.ol TREATMENTS localhost:8080 .
```

### Inital Creation of Treatments and Appointments

For the initial creation of available treatments and booked appointments, go to the /scripts folder.
Executing the jolie_save_treatments.sh script will create 5 different treatments and is called by:
```bash
user:~/scripts$ ./jolie_save_treatments.sh 
```
The the jolie_save_appointments.sh script, will create 5 appointments and is called by: 
```bash
user:~/scripts$ ./jolie_save_appointments.sh 
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
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  	  "customer_name": "<name>",
  	  "id": <id>,
  	  "date": "<date>",
  	  "start_time": <start>,
  	  "end_time": <end>,
  	  "treatment_id": <treatment_id>
}' 
```
Start time and end time only accept integer values between 8 and 18 and the date has to be in the format DD.MM.YYYY.
If your appointment has no conflicts with other appointments and fullfills all constraints (minduration <= duration => maxduration and start_time < end_time) 
it will be confirmed by a pop-up, created by the Confirmation microservice, otherwise you will receive an error message on the commandline.

#### Further Functions

Apart from the above use case, also some additional functions are offered.

##### Treatments

If you want to get the information only for one specific treatment, you have to send a GET request with the ID of the desired treatment to the treatment service.
```bash
$ curl "http://localhost:8080/treatments/treatment/<id>" 
```

If you want to create a new treatment you have to send a POST request to the treatment service, including all required fields.
```bash
$ curl -X "POST" "http://localhost:8080/treatments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
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
$ curl "http://localhost:8081/appointments/list" 
```

If you only want to view the details of a specific appointment, you have to send a GET request to the appointment service, including the ID of your appointment.
```bash
$ curl "http://localhost:8081/appointments/appointment/<id>" 
```