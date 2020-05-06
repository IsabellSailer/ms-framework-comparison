package main

import (
	"context"
	"fmt"
	"log"

	"github.com/micro/go-micro/server"
	"github.com/micro/go-plugins/wrapper/monitoring/prometheus"

	proto "beautysalon/confirmation/proto"

	micro "github.com/micro/go-micro"
)

// Confirms a new Appointment
// :param ctx: context
// :param appointment: newly created Appointment
func ConfirmAppointment(ctx context.Context, appointment *proto.Appointment) error {
	msg := "Thank you  " + appointment.CustomerName + "!\nYour appointment for " + appointment.TreatmentName + "\nwill be on " + appointment.Date + "\nfrom " + fmt.Sprint(appointment.StartTime) + ":00h to " + fmt.Sprint(appointment.EndTime) + ":00h."
	fmt.Println(msg)
	return nil
}

func main() {
	service := micro.NewService(
		micro.Name("beautysalon.confirmation"),
		micro.WrapHandler(prometheus.NewHandlerWrapper()),
		micro.Address(":8082"),
	)

	service.Init()

	micro.RegisterSubscriber("confirm.appointment", service.Server(), ConfirmAppointment, server.SubscriberQueue("queue.pubsub"))

	if err := service.Run(); err != nil {
		log.Fatal(err)
	}
}
