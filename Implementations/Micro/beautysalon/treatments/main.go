package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"log"
	"net/http"
	"strings"

	"github.com/gomodule/redigo/redis"
	rejson "github.com/nitishm/go-rejson"

	"github.com/micro/go-micro/web"
)

type Treatment struct {
	Id          string `json:"id"`
	Name        string `json:"name"`
	Price       string `json:"price"`
	Minduration int32  `json:"minduration"`
	Maxduration int32  `json:"maxduration"`
}

type RedisConnection struct {
	db redis.Conn
}

// Creates a connection to a Redis Database
// @return: Redis Database Connection
func redisConn() redis.Conn {
	var addr = flag.String("Server", "treatments-db:6379", "Redis server address")

	rh := rejson.NewReJSONHandler()
	flag.Parse()

	conn, err := redis.Dial("tcp", *addr)
	if err != nil {
		log.Fatalf("Failed to connect to redis-server @ %s", *addr)
		fmt.Println(err)
	} else {
		fmt.Println("Connected to Redis-Server @", *addr)
	}
	rh.SetRedigoClient(conn)

	return conn
}

// Saves a new Treatment
// @param response: http response
// @param request: http request with new Treatment that should be saved
// @return 201 Detailed information for newly created Treatment
// @return 400 Bad Request
// @return 500 Connection Error to Database
func (conn *RedisConnection) CreateTreatment(response http.ResponseWriter, request *http.Request) {

	decoder := json.NewDecoder(request.Body)
	var treatment Treatment
	err := decoder.Decode(&treatment)
	if err != nil {
		http.Error(response, "Could not create Treatment.", http.StatusBadRequest)
		return
	}

	t, err := json.Marshal(treatment)
	if err != nil {
		http.Error(response, "Could not create Treatment.", http.StatusBadRequest)
		return
	}

	id := treatment.Id
	_, err = conn.db.Do("HSET", id, "JSON", string(t))
	if err == io.EOF {
		http.Error(response, "Could not create Treatment.", http.StatusInternalServerError)
		return
	}

	response.Header().Set("Content-Type", "application/json")
	response.WriteHeader(http.StatusCreated)
	response.Write(t)

}

// Returns all Information for the reqeusted Treatment
// @param response: http response
// @param request: http request with ID
// @return 200 Details for the requested Treatment
// @return 404 No Treatment for given ID
// @return 500 Connection Error to Database
func (conn *RedisConnection) GetTreatment(response http.ResponseWriter, request *http.Request) {

	id := strings.TrimPrefix(request.URL.Path, "/treatments/")

	s, err := redis.String(conn.db.Do("HGET", id, "JSON"))
	if err == io.EOF {
		http.Error(response, "Could not read Treatment.", http.StatusInternalServerError)
		return
	}

	treatment := &Treatment{}
	err = json.Unmarshal([]byte(s), treatment)
	if err != nil {
		http.Error(response, "Could not find a Treatment for provided ID.", http.StatusNotFound)
		return
	}

	t, err := json.Marshal(treatment)
	if err != nil {
		http.Error(response, "Could not find a Treatment for provided ID.", http.StatusNotFound)
		return
	}

	response.Write(t)

}

// Shows a list of all offered Treatments
// @param response: http response
// @param request: http request
// @return 200 List of all offered Treatments
// @return 404 No stored Treatments
// @return 500 Connection Error to Database
func (conn *RedisConnection) GetTreatments(response http.ResponseWriter, request *http.Request) {

	keys, err := redis.Strings(conn.db.Do("KEYS", "*"))
	if err == io.EOF {
		http.Error(response, "Could not read Treatments.", http.StatusInternalServerError)
		return
	}

	if len(keys) == 0 {
		http.Error(response, "Currently there are no Treatments offered.", http.StatusNotFound)
		return
	}

	for _, key := range keys {
		s, err := redis.String(conn.db.Do("HGET", key, "JSON"))
		if err != nil {
			http.Error(response, "Could not read Treatment.", http.StatusInternalServerError)
			return
		}

		treatment := &Treatment{}
		err = json.Unmarshal([]byte(s), treatment)
		if err != nil {
			http.Error(response, "Could not find a Treatment for provided ID.", http.StatusNotFound)
			return
		}

		t, err := json.Marshal(treatment)
		if err != nil {
			http.Error(response, "Could not find a Treatment for provided ID.", http.StatusNotFound)
			return
		}
		response.Write(t)
	}
}

func main() {

	service := web.NewService(
		web.Name("beautysalon.com"),
		web.Address(":8080"),
	)

	redconn := redisConn()
	conn := &RedisConnection{db: redconn}

	service.HandleFunc("/treatments", conn.CreateTreatment)
	service.HandleFunc("/treatments/", conn.GetTreatment)
	service.HandleFunc("/treatments/list", conn.GetTreatments)

	if err := service.Init(); err != nil {
		log.Fatal(err)
	}

	if err := service.Run(); err != nil {
		log.Fatal(err)
	}
}
