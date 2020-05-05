package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"strings"

	"github.com/go-pg/pg"
	"github.com/go-pg/pg/orm"

	"github.com/micro/go-micro/client"
	"github.com/micro/go-micro/config"
	"github.com/micro/go-micro/web"

	micro "github.com/micro/go-micro"
	breaker "github.com/micro/go-plugins/wrapper/breaker/gobreaker"

	proto "beautysalon/appointments/proto"
)

type Appointment struct {
	Id            string `json:"id"`
	CustomerName  string `json:"customer_name"`
	Date          string `json:"date"`
	StartTime     int32  `json:"start_time"`
	EndTime       int32  `json:"end_time"`
	Duration      int32  `json:"duration"`
	TreatmentId   string `json:"treatment_id"`
	TreatmentName string `json:"treatment_name"`
}

type Treatment struct {
	Id          string `json:"id"`
	Name        string `json:"name"`
	Price       string `json:"price"`
	Minduration int32  `json:"minduration"`
	Maxduration int32  `json:"maxduration"`
}

type Connections struct {
	db     *pg.DB
	client *http.Client
	pub    micro.Publisher
}

type Database struct {
	Addr         string `json:"address"`
	User         string `json:"user"`
	Password     string `json:"password"`
	DatabaseName string `json:"databasename"`
}

// Creates a connection to a PostgreSQL Database
// @return: Postgres Database Connection
func postgresConn() *pg.DB {
	config.LoadFile("./config.json")

	var database Database
	config.Get("databases", "postgres").Scan(&database)

	db := pg.Connect(&pg.Options{
		Addr:     database.Addr,
		User:     database.User,
		Password: database.Password,
		Database: database.DatabaseName,
	})

	err := createSchema(db)
	if err != nil {
		panic(err)
	}

	return db
}

// Creates the Scheme for the PostgreSQL Database
// param db: Postgres Database
func createSchema(db *pg.DB) error {
	for _, model := range []interface{}{(*Appointment)(nil)} {
		err := db.CreateTable(model, &orm.CreateTableOptions{
			IfNotExists: true,
		})
		if err != nil {
			return err
		}
	}
	return nil
}

// Saves a new Appointment
// @param response: detailed information for newly created Appointment
// @param request: new Appointment that should be saved
// @return 201 Detailed information for newly created Appointment StatusConflict
// @return 400 Bad Request
// @return 404 No Treatment for given TreatmentID
// @return 409 Appointment Details do not match the requirements
// @return 500 Connection Error
func (conn *Connections) CreateAppointment(response http.ResponseWriter, request *http.Request) {

	decoder := json.NewDecoder(request.Body)
	var appointment Appointment
	err := decoder.Decode(&appointment)
	if err != nil {
		http.Error(response, "Could not create Appointment.", http.StatusBadRequest)
		return
	}

	if appointment.StartTime >= appointment.EndTime {
		http.Error(response, "Start time has to be before end time.", http.StatusConflict)
		return
	} else if appointment.StartTime < 8 || appointment.StartTime > 17 || appointment.EndTime < 9 || appointment.EndTime > 18 {
		http.Error(response, "Appointments are only available from 8 - 18. Please choose another timeslot.", http.StatusConflict)
		return
	}

	link := "http://beautysalon.com/treatments/" + appointment.TreatmentId
	treatresp, err := conn.client.Get(link)
	if err != nil {
		http.Error(response, "Could not read Treatment.", http.StatusInternalServerError)
		return
	}

	defer treatresp.Body.Close()
	t, err := ioutil.ReadAll(treatresp.Body)
	if err != nil {
		http.Error(response, "Could not read Treatment for given TreatmentID.", http.StatusNotFound)
		return
	}

	treatment := &Treatment{Id: appointment.TreatmentId}
	err = json.Unmarshal([]byte(t), treatment)
	if err != nil {
		http.Error(response, "Could not read Treatment for given TreatmentID.", http.StatusNotFound)
		return
	}

	if treatment.Minduration > appointment.EndTime-appointment.StartTime {
		http.Error(response, "An Appointment for "+treatment.Name+" takes at least "+fmt.Sprint(treatment.Minduration)+" hour(s). Please choose another timeslot.\n", http.StatusConflict)
		return
	} else if treatment.Maxduration < appointment.EndTime-appointment.StartTime {
		http.Error(response, "An Appointment for "+treatment.Name+" takes maximum "+fmt.Sprint(treatment.Maxduration)+" hour(s). Please choose another timeslot.\n", http.StatusConflict)
		return
	}

	var conflicts int
	conflicts = 0
	var appointments []Appointment
	err = conn.db.Model(&appointments).Select()
	if err != nil {
		http.Error(response, "Could not read Appointments.", http.StatusInternalServerError)
		return
	}

	for _, ap := range appointments {
		if treatment.Name == ap.TreatmentName {
			if ap.Date == appointment.Date {
				if !(appointment.StartTime <= ap.StartTime && appointment.EndTime <= ap.StartTime) && !(ap.EndTime <= appointment.EndTime && ap.EndTime <= appointment.StartTime) {
					conflicts += 1
				}
			}
		}
	}

	if conflicts != 0 {
		http.Error(response, "There were "+strconv.Itoa(conflicts)+" conflicts with other Appointments. Please choose another timeslot.", http.StatusConflict)
		return
	}

	app := &Appointment{
		Id:            appointment.Id,
		CustomerName:  appointment.CustomerName,
		Date:          appointment.Date,
		StartTime:     appointment.StartTime,
		EndTime:       appointment.EndTime,
		Duration:      appointment.EndTime - appointment.StartTime,
		TreatmentId:   appointment.TreatmentId,
		TreatmentName: treatment.Name,
	}
	err = conn.db.Insert(app)
	if err != nil {
		http.Error(response, "Could not save Appointment.", http.StatusInternalServerError)
		return
	}

	response.Header().Set("Content-Type", "application/json")
	response.WriteHeader(http.StatusCreated)
	a, err := json.Marshal(app)
	if err != nil {
		http.Error(response, "Could not send Appointment Confirmation.", http.StatusInternalServerError)
		return
	}

	confapp := &proto.Appointment{
		Id:            appointment.Id,
		CustomerName:  appointment.CustomerName,
		Date:          appointment.Date,
		StartTime:     appointment.StartTime,
		EndTime:       appointment.EndTime,
		Duration:      appointment.EndTime - appointment.StartTime,
		TreatmentId:   appointment.TreatmentId,
		TreatmentName: treatment.Name,
	}

	if err := conn.pub.Publish(context.Background(), confapp); err != nil {
		http.Error(response, "Could not send Appointment Confirmation.", http.StatusInternalServerError)
	}

	response.Write(a)
}

// Returns all Information for the reqeusted Appointment
// @param response: http response with details for the requested Appointment
// @param request: http request with ID
// @return 200 Details for the requested Appointment
// @return 404 No Appointment for given ID
// @return 500 Connection Error to Database
func (conn *Connections) GetAppointment(response http.ResponseWriter, request *http.Request) {

	id := strings.TrimPrefix(request.URL.Path, "/appointments/")

	appointment := &Appointment{Id: id}
	err := conn.db.Select(appointment)
	if err == io.EOF {
		http.Error(response, "Could not read Appointment.", http.StatusInternalServerError)
		return
	}

	if appointment.CustomerName == "" {
		http.Error(response, "Could not find an Appointment for provided ID.", http.StatusNotFound)
		return
	}

	a, err := json.Marshal(appointment)
	if err != nil {
		http.Error(response, "Could not find an Appointment for provided ID.", http.StatusNotFound)
		return
	}

	response.Write(a)
}

// Shows a list of all settled Appointments
// @param response: http response with list of all settled Appointments
// @param request: http request
// @return 200 List of all offered Appointments
// @return 404 No stored Appointments
// @return 500 Connection Error to Database
func (conn *Connections) GetAppointments(response http.ResponseWriter, request *http.Request) {

	var appointments []Appointment
	err := conn.db.Model(&appointments).Select()
	if err == io.EOF {
		http.Error(response, "Could not read Appointments.", http.StatusInternalServerError)
		return
	}

	if len(appointments) == 0 {
		http.Error(response, "No Appointments exist.", http.StatusNotFound)
		return
	}

	for _, appointment := range appointments {
		a, err := json.Marshal(appointment)
		if err != nil {
			http.Error(response, "Could not read Appointments.", http.StatusInternalServerError)
			return
		}
		response.Write(a)
	}
}

func main() {

	webservice := web.NewService(
		web.Name("appointmentsservice.com"),
		web.Address(":8081"),
	)

	ec := client.NewClient(
		client.Wrap(breaker.NewClientWrapper()),
	)

	wc := webservice.Client()
	postconn := postgresConn()
	pub := micro.NewPublisher("confirm.appointment", ec)
	conn := &Connections{db: postconn, client: wc, pub: pub}

	webservice.HandleFunc("/appointments", conn.CreateAppointment)
	webservice.HandleFunc("/appointments/", conn.GetAppointment)
	webservice.HandleFunc("/appointments/list", conn.GetAppointments)

	if err := webservice.Init(); err != nil {
		log.Fatal(err)
	}

	if err := webservice.Run(); err != nil {
		log.Fatal(err)
	}
}
