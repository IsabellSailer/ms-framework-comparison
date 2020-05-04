include "console.iol"
include "ui/swing_ui.iol"
include "./public/interfaces/ConfirmationInterface.iol"

execution{ concurrent }

inputPort CONFIRMATION {
    Location: "socket://localhost:8082"
    Protocol: sodep
    Interfaces: ConfirmationInterface
}

main {

    [ confirmAppointment( request ) ]{ 
        println@Console("Thank you " + request.customer_name + "!\nYour appointment for \n" + request.treatment_name + "\nwill be on " + request.date + "\nfrom " + request.start_time + ":00h to " + request.end_time + ":00h." )()
    }

}
