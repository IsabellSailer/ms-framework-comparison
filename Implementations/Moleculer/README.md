# Beauty Salon - Moleculer

The services can be run either completely in Docker or with the services locally and only the supporting services in Docker:

* [Docker only](#Docker-only)
* [Services locally](#Services-locally)

To test the services with sample requests, you can use the scripts at [scripts](https://github.com/IsabellSailer/ms-framework-comparison/tree/master/Implementations/scripts). Further information can be found here:

* [How to use the API](#How-to-use-the-API)

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
$ docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3
$ docker run --name treatments-db -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=docker -p 27017:27017 -d mongo:4
$ docker run --name appointments-db -d -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments postgres:12-alpine
```

Second, you can build and run the services:

#### Treatments service

```bash
user:~/beautysalon/treatments$ docker build -t beautysalon-moleculer/treatments:1.0 .
user:~/beautysalon/treatments$ docker run --name treatments --link treatments-db --link rabbitmq -p "8080:8080" -d beautysalon-moleculer/treatments:1.0
```

#### Appointments service

```bash
user:~/beautysalon/appointments$ docker build -t beautysalon-moleculer/appointments:1.0 .
user:~/beautysalon/appointments$ docker run --name appointments --link appointments-db --link rabbitmq -p "8081:8081" -d beautysalon-moleculer/appointments:1.0
```

#### Confirmation service

```bash
user:~/beautysalon/confirmation$ docker build -t beautysalon-moleculer/confirmation:1.0 .
user:~/beautysalon/confirmation$ docker run --name confirmation --link rabbitmq -d beautysalon-moleculer/confirmation:1.0
```

## Services locally

To run the services locally, you can use the following instructions to set up your local development environment:

### Environment

The Application was developed on Ubuntu 18.04, so this guide provides commands for Debian based OS.
Any other Unix-like OS would work as well (commands may be different).

### Requirements

Make sure you are logged in as an user with sudo privileges for installation.

#### Install Node.JS

```bash
curl -sL https://deb.nodesource.com/setup_14.x | sudo -E bash -
sudo apt-get install -y nodejs
```

#### Docker

```bash
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```

### Running the Application

#### Starting the supporting services

Before running the application, you have to start RabbitMQ, MongoDB and PostgreSQL

```bash
$ sudo docker run -d -p 5672:5672 -p 15672:15672 rabbitmq:3
```

```bash
$ sudo docker run -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=docker -p 27017:27017 -d mongo:4
```

```bash
$ sudo docker run -d -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments postgres:12-alpine
```

#### Starting the services

The commands for running the three microservices of the Beauty Salon application are:

```bash
user:~/beautysalon/treatments$ npm install
user:~/beautysalon/treatments$ npm start
```

```bash
user:~/beautysalon/appointments$ npm install
user:~/beautysalon/appointments$ npm start
```

```bash
user:~/beautysalon/confirmation$ npm install
user:~/beautysalon/confirmation$ npm start
```

Or, for each service to let the code hot reload, you can use:

```bash
user:~/beautysalon/<servicename>$ npm run dev
```

## How to use the API

### Initial Creation of Treatments and Appointments
For the initial creation of available treatments and booked appointments, go to the /scripts folder.
Executing the moleculer_save_treatments.sh script will create 5 different treatments and is called by:

```bash
user:~/scripts ./moleculer_save_treatments.sh <port-number>
```

As argument you have to pass the port number on which the treatment microservice is listening.
For the treatments service, the port is set to 8080.

The same holds for the moleculer_save_appointments.sh script, which creates 5 appointments and is called by:

```bash
user:~/scripts ./moleculer_save_appointments.sh <port-number>
```

For the appointments service, the port number is set to 8081.

### How to use the Application 

After starting the microservices, you can communicate with the treatments and the appointments services.

#### Use Case Example
In our example use case a customer wants to book an appointment for a specific treatment. 
As for creating an appointment the ID of the desired treatment is needed, you have to get the IDs of the offered treatments first.
To view the list of all available treatments and their IDs open a new terminal and enter the command:
```bash
$ curl 'http://localhost:<port-nr>/treatments' 
```
It will return a list with all available treatments including all important information: ID, Name, Price, Minduration and Maxduration.

For creating an appointment you have to send a POST request to the appointments service, containing the required fields for an appointment, including the ID of the desired treatment.

```bash
$ curl -X "POST" "http://localhost:<port-nr>/appointments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "customerName": "<name>",
  "date": "<date>",
  "startTime": <start>,
  "endTime": <end>,
  "treatmentId": <id>
}' 
```

Start time and end time only accept integer values between 8 and 18 and the date has to be in the format DD.MM.YYYY.
If your appointment has no conflicts with other appointments and fullfills all constraints (minduration <= duration => maxduration and start_time < end_time) 
it will be confirmed by a console log in the Confirmation service, otherwise you will receive an error message on the commandline.

#### Further Functions

Apart from the above use case, also some additional functions are offered.

##### Treatments

If you want to get the information only for one specific treatment, you have to send a GET request with the ID of the desired treatment to the treatment service.
```bash
$ curl 'http://localhost:<port-nr>/treatments/<id>' " 
```

If you want to create a new treatment you have to send a POST request to the treatment service, including all required fields.

```bash
$ curl -X "POST" "http://localhost:<port-nr>/treatments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -H 'Authorization: Basic YWRtaW46YWRtaW4=' \
     -d $'{
  "id": <id>,
  "name": "<name>",
  "price": <price>,
  "minduration": <minduration>,
  "maxduration": <maxduration>
}' 
```

##### Appointments

If you want to view all booked appointments including their information you have to send a GET request to the appointment service.
```bash
$ curl 'http://localhost:<port-nr>/appointments' 
```

If you only want to view the details of a specific appointment, you have to send a GET request to the appointment service, including the ID of your appointment.
```bash
$ curl 'http://localhost:<port-nr>/appointments/<id>' 
```