# Beauty Salon

## Environment
The Application was developed on Ubuntu 18.04, so this guide provides commands for Debian based OS. 
Any other Unix-like OS would work as well (commands may be different).


## Requirements
Make sure you are logged in as an user with sudo privileges for installation. 

### JDK Version 11
```bash
$ sudo apt install openjdk-11-jdk
```

### SDKMAN
```bash
$ curl -s "https://get.sdkman.io" | bash
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
```
#### Micronaut 1.2.1

```bash
$ sdk install micronaut 1.2.1
```
#### Gradle 5.6.2
```bash
$ sdk install gradle 5.6.2
```
### Docker
```bash
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```
#### Consul 1.2.4
```bash
$ sudo docker pull consul:1.2.4
```
#### RabbitMQ 3.7.11 Management
```bash
$ sudo docker pull rabbitmq:3.7.11-management
```
#### Postgres 10.5 Alpine
```bash
$ sudo docker pull postgres:10.5-alpine
```
#### Redis 5
```bash
$ sudo docker pull redis:5
```
#### OpenZipkin/Zipkin 2.17.0
```bash
$ sudo docker pull openzipkin/zipkin:2.17.0
```

## Running the Application
### Starting the Container
Before running the application, you have to start Consul, RabbitMQ, PostgreSQL, Redis and Zipkin.
```bash
$ sudo docker run -p 8500:8500 consul:1.2.4
```
```bash
$ sudo docker run --rm -it \
        -p 5672:5672 \
        -p 15672:15672 \
        rabbitmq:3.7.11-management
```
```bash
$ docker run -it \
    -p 5432:5432 \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=docker \
    -e POSTGRES_DB=appointments \
    postgres:10.5-alpine
```
```bash
$ sudo docker run --name treatmentsdb -d redis:5
```
```bash
$ sudo docker run -d -p 9411:9411 openzipkin/zipkin:2.17.0
```
Now you can access Consuls UI by calling http://localhost:8500 
and Zipkins UI by calling http://localhost:9411 .

### Starting the Microservices
Go to the root folder of the project, there you can start a microservice with the command:
```bash
user:~/beautysalon$ ./gradlew <microservice-name>::run
```
The command for running the 3 microservices of the Beauty Salon application are:
```bash
user:~/beautysalon$ ./gradlew treatments::run
```
```bash
user:~/beautysalon$ ./gradlew appointments::run
```
```bash
user:~/beautysalon$ ./gradlew confirmation::run
```
## How to get the API

After starting a microservice its API is accessable under:
```
http://localhost:<port-number>/swagger/beauty-salon---<microservice-name>-1.0.yml
```

### Initial Creation of Treatments and Appointments
For the initial creation of available treatments and booked appointments, go to the /scripts folder.
Executing the micronaut_save_treatments.sh script will create 5 different treatments and is called by:
```bash
user:~/scripts ./micronaut_save_treatments.sh <port-number>
```
As argument you have to pass the port number on which the treatment microservice is listening. 
You can get this port for example from Consul.
The same holds for the micronaut_save_appointments.sh script, which creates 5 appointments and is called by: 
```bash
user:~/scripts ./micronaut_save_appointments.sh <port-number>
```

### How to use the Application 

After starting the microservices, you can communicate with the treatments and the appointments services.

#### Use Case Example
In our example use case a customer wants to book an appointment for a specific treatment. 
As for creating an appointment the ID of the desired treatment is needed, you have to get the IDs of the offered treatments first.
To view the list of all available treatments and their IDs open a new terminal and enter the command:
```bash
$ curl 'http://localhost:<port-nr>/treatments/list' 
```
It will return a list with all available treatments including all important information: ID, Name, Price, Minduration and Maxduration.

For creating an appointment you have to send a POST request to the appointments service, containing the required fields for an appointment, including the ID of the desired treatment.
```bash
$ curl -X "POST" "http://localhost:<port-nr>/appointments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "customerName": "<name>",
  "date": "<date>",
  "startTime": "<start>",
  "endTime": "<end>",
  "treatmentId": "<id>"
}' 
```
Start time and end time only accept integer values between 8 and 18 and the date has to be in the format DD.MM.YYYY.
If your appointment has no conflicts with other appointments and fullfills all constraints (minduration <= duration => maxduration and start_time < end_time) 
it will be confirmed by a pop-up, created by the Confirmation service, otherwise you will receive an error message on the commandline.

#### Further Functions

Apart from the above use case, also some additional functions are offered.

##### Treatments

If you want to get the information only for one specific treatment, you have to send a GET request with the ID of the desired treatment to the treatment service. Since the function can only be used by authorized users you additionally have to send an authorization Token within the Header.
```bash
$ curl 'http://localhost:<port-nr>/treatments/<id>' -H "Authorization: Basic YWRtaW46YWRtaW4=" 
```

If you want to create a new treatment you have to send a POST request to the treatment service, including all required fields.
Since in a real world scenario only the owner/employees of a beauty salon should be able to create new treatments, the function can only be used by authorized people.
Therefore you need to authorize yourself by sending a valid token in the header of your request.
Additionally you have to specify in the header of your request which API-Version you want to use. You can choose between 1 (asynchronous) and 2 (synchronous).
```bash
$ curl -X "POST" "http://localhost:<port-nr>/treatments/" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -H "Authorization: Basic YWRtaW46YWRtaW4=" \
     -H "X-API-VERSION: <version>" \
     -d $'{
  "id": "<id>",
  "name": "<name>",
  "price": "<price>",
  "minduration": "<minduration>",
  "maxduration": "<maxduration>"
}' 
```

##### Appointments

If you want to view all booked appointments including their information you have to send a GET request to the appointment service.
```bash
$ curl 'http://localhost:<port-nr>/appointments/list' 
```

If you only want to view the details of a specific appointment, you have to send a GET request to the appointment service, including the ID of your appointment.
```bash
$ curl 'http://localhost:<port-nr>/appointments/<id>' 
```