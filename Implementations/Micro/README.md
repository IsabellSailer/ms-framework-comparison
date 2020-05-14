# Beauty Salon - Micro

The services can be run either completely in Docker or with the services locally and only the supporting services in Docker:

* [Docker only](#Docker-only)
* [Services locally](#Services-locally)

To test the services with sample requests, you can use the scripts at [scripts](https://github.com/IsabellSailer/ms-framework-comparison/tree/master/Implementations/scripts). Further information can be found here:

* [How to use the Application](#How-to-use-the-application)

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
user:~/beautysalon/treatments$ docker build -t beautysalon-micro/treatments:1.0 .
user:~/beautysalon/treatments$ docker run --name treatments --link treatments-db -p 8080:8080 beautysalon-micro/treatments:1.0
```

#### Appointments service

```bash
user:~/beautysalon/appointments$ docker build -t beautysalon-micro/appointments:1.0 .
user:~/beautysalon/appointments$ docker run --name appointments --link appointments-db --link treatments beautysalon-micro/appointments:1.0
```

#### Confirmation service

```bash
user:~/beautysalon/confirmation$ docker build -t beautysalon-micro/confirmation:1.0 .
user:~/beautysalon/confirmation$ docker run --name confirmation beautysalon-micro/confirmation:1.0
```

Third, you have to start prometheus, because it has to have access to the confirmation service:

```bash
$ docker run -d --name prometheus -p 9090:9090 --link confirmation -v ./prometheus-docker.yml:/etc/prometheus/prometheus.yml --restart on-failure prom/prometheus:v2.13.1 --config.file=/etc/prometheus/prometheus.yml
```

## Services locally

To run the services locally, you can use the following instructions to set up your local development environment:

### Environment

The Application was developed on Ubuntu 18.04, so this guide provides commands for Debian based OS. 
Any other Unix-like OS would work as well (commands may be different).

### Requirements

Make sure you are logged in as an user with sudo privileges for installation. 

#### Go 1.13

```bash
$ sudo add-apt-repository ppa:longsleep/golang-backports
$ sudo apt-get update
$ sudo apt-get install golang-go
```

#### Setting $GOPATH

Edit your ~/.bash_profile file and append at the end:

```bash
export GOPATH=$HOME/go
```

for sourcing the .bash_profile automatically you have to edit the ~/.bashrc file and append at the end:

```bash
source ~/.bash_profile
```

#### Micro Runtime

```bash
user:~/go$ go get github.com/micro/micro
```

#### Go modules and Go-Micro modules

The go modules are already referenced, but you can make sure all modules are available for each service by running:

```bash
user:~/beautysalon/treatments$ go mod install
user:~/beautysalon/appointments$ go mod install
user:~/beautysalon/confirmation$ go mod install
```

For using messaging with micro you need protobuf code generation for micro, therefore you first have to install Protobuf and protoc generation for micro.

#### Protobuf

```bash
$ sudo apt install linuxbrew-wrapper
$ brew install protobuf
```

#### Proto & Protoc-Gen-Go
```bash
user:~/go$ go get -u github.com/golang/protobuf/{proto,protoc-gen-go}
```

#### Protoc-Gen-Micro
```bash
user:~/go$ go get -u github.com/micro/protoc-gen-micro
```

If you get an error like:
```bash
panic: /debug/requests is already registered. You may have two independent copies of golang.org/x/net/trace in your binary, trying to maintain separate state. This may involve a vendored copy of golang.org/x/net/trace.

goroutine 1 [running]:
go.etcd.io/etcd/vendor/golang.org/x/net/trace.init.0()
	/home/isabell/go/src/go.etcd.io/etcd/vendor/golang.org/x/net/trace/trace.go:123 +0x1cd

```

Go to ~/go/src/go.etcd.io/etcd/vendor/golang.org/x/net and delete the folder trace. Also go to ~/go/src/github.com/coreos/etcd/vendor/golang.org/x/net and delete the trace folder as well.

### Directory of the Beautysalon

#### Docker

```bash
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```

#### PostgreSQL 12

```bash
$ sudo docker pull postgres:12-alpine
```

#### Redis 5

```bash
$ sudo docker pull redis:5
```

#### Prometheus

Download the latest release from prometheus.io, here Version 2.13.1 was used. Then extract it, e.g. with:
```bash
user:~/downloads$ tar xvfz prometheus-*.tar.gz
```

For monitoring the Confirmations microservice, you have to configure the prometheus.yml file, located at the root of your extracted Prometheus directory.
Replace the default scrape_configs with:
```bash
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'beautysalon-confirmation'

    # Override the global default and scrape targets from this job every 10 seconds.
    scrape_interval: 10s

    static_configs:
      - targets: ['localhost:8082']
```

### Running the Application

### Starting the supporting services

Before running the application, you have to start PostgreSQL and Redis.

```bash
$ sudo docker run -d -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments -p 5432:5432 postgres:12-alpine
```

```bash
$ sudo docker run  -d -p 6379:6379 redis:5
```

### Starting Prometheus

Additionally you have to start Prometheus.
```bash
user:~/prometheus-2.13.1.linux-amd64$ ./prometheus --config.file=prometheus.yml
```

Now you can access Prometheus UI via http://localhost:9090 .

### Starting the Microservices

Go to the root folder of the wanted microservice and start it.
```bash
user:~/beautysalon/treatments$ go run main.go
```

```bash
user:~/beautysalon/appointments$ go run main.go
```

For sending the metrics to Prometheus, you have to specify the port for the confirmation microservice.
```bash
user:~/beautysalon/confirmation$ go run main.go --server_address 127.0.0.1:8082
```

### Inital Creation of Treatments and Appointments

For the initial creation of available treatments and booked appointments, go to the /scripts folder.
For avoiding problems, it is recommended to execute these scripts before starting the confirmation service.
Executing the micro_save_treatments.sh script will create 5 different treatments and is called by:
```bash
user:~/scripts ./micro_save_treatments.sh <port-number>
```
As argument you have to pass the port number on which the treatment microservice is listening. 
You can get this port for example from the command line after starting the service.
The same applies for the micro_save_appointments.sh script, which creates 5 appointments and is called by: 
```bash
user:~/scripts ./micro_save_appointments.sh <port-number>
``` 

### How to use the Application

After starting the microservices, you can communicate with the treatments and the appointments services.

#### Use Case Example

In our example use case a customer wants to book an appointment for a specific treatment. 
As for creating an appointment the ID of the desired treatment is needed, you have to get the IDs of the offered treatments first.
To view the list of all available treatments and their IDs open a new terminal and enter the command:
```bash
$ curl "http://localhost:<port-nr>/treatments/list" 
```
It will return a list with all available treatments including all important information: ID, Name, Price, Minduration and Maxduration.

For creating an appointment you have to send a POST request to the appointments service, containing the required fields for an appointment, including the ID of the desired treatment.
```bash
$ curl -X "POST" "http://localhost:<port-nr>/appointments" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  	  "customerName": "<name>",
  	  "date": "<date>",
  	  "start_time": <start>,
  	  "end_time": <end>,
  	  "treatment_id": "<id>",
  	  "id": "<id>"
}' 
```

Start time and end time only accept integer values between 8 and 18 and the date has to be in the format DD.MM.YYYY.
If your appointment has no conflicts with other appointments and fullfills all constraints (minduration <= duration => maxduration and start_time < end_time) 
it will be confirmed by a console log in the Confirmation microservice, otherwise you will receive an error message on the commandline.


#### Further Functions

Apart from the above use case, also some additional functions are offered.

##### Treatments

If you want to get the information only for one specific treatment, you have to send a GET request with the ID of the desired treatment to the treatment service.
```bash
$ curl 'http://localhost:<port-nr>/treatments/<id>' 
```

If you want to create a new treatment you have to send a POST request to the treatment service, including all required fields.
```bash
$ curl -X "POST" "http://localhost:<port-nr>/treatments" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "id": "<id>",
  "name": "<name>",
  "price": "<price>",
  "minduration": <minduration>,
  "maxduration": <maxduration>
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