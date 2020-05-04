# Beauty Salon

## Environment
The Application was developed on Ubuntu 18.04, so this guide provides commands for Debian based OS. 
Any other Unix-like OS would work as well (commands may be different).


## Requirements
Make sure you are logged in as an user with sudo privileges for installation. 

### Go 1.13

```bash
$ sudo add-apt-repository ppa:longsleep/golang-backports
$ sudo apt-get update
$ sudo apt-get install golang-go
```

#### Setting $GOPATH
Edit your ~/.bash_profile file and append at the end :
```bash
export GOPATH=$HOME/go
```
for sourcing the .bash_profile automatically you have to edit the ~/.bashrc file and append at the end:
```bash
source ~/.bash_profile
```

### Go-Micro 1.11.1
First make sure that $GOROOT is not set, otherwise unset the variable.

```bash
user:~$ echo $GOROOT
/usr/local/go
user:~$ unset GOROOT
user:~$ echo $GOROOT
user:~$ 
```

To make sure it is unset permanently, add to your ~/.bash_profile file the line:
```bash
unset GOROOT
```

For installing packages you have to be at $GOPATH. 

```bash
user:~$ cd /go
user:~/go$ go get github.com/micro/go-micro
```

If you receive an error output like:
```bash
../../github.com/coreos/etcd/clientv3/auth.go:121:72: cannot use auth.callOpts (type []"github.com/coreos/etcd/vendor/google.golang.org/grpc".CallOption) as type []"go.etcd.io/etcd/vendor/google.golang.org/grpc".CallOption in argument to auth.remote.AuthEnable
```

you have to customize the files watcher.go and etcd.go in ~/go/src/github.com/micro/go-micro/registry/etcd .
Therefore replace the import from "github.com/coreos/etcd/clientv3" to "go.etcd.io/etcd/clientv3" and the error should be fixed.

### Micro Runtime
```bash
user:~/go$ go get github.com/micro/micro
```

### Go Modules
```bash

user:~/go$ go get github.com/gomodule/redigo/redis
user:~/go$ go get github.com/nitishm/go-rejson
user:~/go$ cd $GOPATH/src/github.com/nitishm/go-rejson
user:~/go/src/github.com/nitishm/go-rejson$ ./install-redis-rejson.sh
user:~/go$ go get github.com/go-pg/pg
user:~/go$ sudo apt-get install libglib2.0-dev libgtksourceview2.0-dev libgtk-3-dev
user:~/go$ go get github.com/gotk3/gotk3/gtk

```

### Micro Modules
```bash
user:~/go$ go get github.com/micro/go-plugins
user:~/go$ go get github.com/micro/go-micro/web
user:~/go$ go get github.com/micro/go-micro/config
user:~/go$ go get github.com/micro/go-plugins/wrapper/breaker/gobreaker
user:~/go$ go get github.com/micro/go-plugins/wrapper/monitoring/prometheus
```

For using messaging with micro you need protobuf code generation for micro, therefore you first have to install Protobuf and protoc generation for micro.

### Protobuf
```bash
$ sudo apt install linuxbrew-wrapper
$ brew install protobuf
```

### Proto & Protoc-Gen-Go
```bash
user:~/go$ go get -u github.com/golang/protobuf/{proto,protoc-gen-go}
```

### Protoc-Gen-Micro
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
For the application to work properly, you have to store the code of the beautysalon under ~/go/src

### Docker
```bash
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```
#### PostgreSQL 12
```bash
$ sudo docker pull postgres:12
```
#### Redis 5
```bash
$ sudo docker pull redis:5
```

### Prometheus
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
      - targets: ['localhost:8081']
```

## Running the Application

### Starting the Container
Before running the application, you have to start PostgreSQL and Redis.
```bash
$ sudo docker run --name appointments -e POSTGRES_PASSWORD=docker -e POSTGRES_DB=appointments -p 5432:5432 -d postgres:12
```
```bash
$ sudo docker run --name treatmentsdb -d -p 6379:6379 redis:5
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
user:~/go/src/beautysalon/treatments$ go run main.go
```
```bash
user:~/go/src/beautysalon/appointments$ go run main.go
```
For sending the metrics to Prometheus, you have to specify the port for the confirmation microservice.
```bash
user:~/go/src/beautysalon/confirmation$ go run main.go --server_address 127.0.0.1:8081
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

### How to use the Application - Example

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
it will be confirmed by a pop-up, created by the Confirmation microservice, otherwise you will receive an error message on the commandline.


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