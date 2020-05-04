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

### Jolie 1.8.2

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


### Docker
```bash
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```
#### PostgreSQL 12
```bash
$ sudo docker pull postgres:12
```
#### MongoDB
```bash
$ sudo docker pull mongo
```


## Running the Application
### Starting the Container
Before running the application, you have to start PostgreSQL and MongoDB. 
```bash
$ sudo docker run --name appointments -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments -p 5432:5432 -d postgres:12
```
```bash
$ sudo docker run --name treatments -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=docker -p 27017:27017 -d mongo
```

### Starting the Microservices
Go to the root folder of the desired microservice, there you can start the service with the commands:
```bash
user:~/beautysalon/treatments$ jolier treatment.ol TREATMENTS localhost:8000
```
```bash
user:~/beautysalon/appointments$ jolier appointment.ol APPOINTMENTS localhost:8001
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
user:~/beautysalon/appointments$ jolie2openapi appointment.ol APPOINTMENTS localhost:8001 . 
```
For the Treatments microservice you run: 
```bash
user:~/beautysalon/appointments$ jolie2openapi treatment.ol TREATMENTS localhost:8000 .
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
$ curl 'http://localhost:8000/treatments/list' 
```
It will return a list with all available treatments including all important information: ID, Name, Price, Minduration and Maxduration.

For creating an appointment you have to send a POST request to the appointments service, containing the required fields for an appointment, including the ID of the desired treatment.
```bash
$ curl -X "POST" "http://localhost:8001/appointments/" 
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
$ curl "http://localhost:8000/treatments/treatment/<id>" 
```

If you want to create a new treatment you have to send a POST request to the treatment service, including all required fields.
```bash
$ curl -X "POST" "http://localhost:8000/treatments/" \
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
$ curl "http://localhost:8001/appointments/list" 
```

If you only want to view the details of a specific appointment, you have to send a GET request to the appointment service, including the ID of your appointment.
```bash
$ curl "http://localhost:8001/appointments/appointment/<id>" 
```