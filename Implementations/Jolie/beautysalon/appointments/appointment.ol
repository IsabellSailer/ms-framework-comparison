include "console.iol"
include "string_utils.iol"
include "database.iol"

include "./public/interfaces/AppointmentInterface.iol"
include "./public/interfaces/TreatmentInterface.iol"
include "./public/interfaces/ConfirmationInterface.iol"

execution{ concurrent }

inputPort APPOINTMENTS {
  Location: "local"
  Protocol: http
  Interfaces: AppointmentInterface
}

outputPort GETTREATMENT {
  Location: "socket://treatments:8090"
  Protocol: http
  Interfaces: TreatmentInterface
}

outputPort CONFIRMATION {
  Location: "socket://confirmation:8082"
  Protocol: sodep
  Interfaces: ConfirmationInterface
}


init {

    with ( connectionInfo ) {
        .username = "postgres";
        .password = "docker";
        .host = "appointments-db";
        .database = "appointments";
        .driver = "postgresql"
    };
    connect@Database( connectionInfo )( void );

    println@Console("Server is up and running...")();
    println@Console("...creating appointments table in the database...")();

    scope ( createTable ) {
        install ( SQLException => println@Console("...nothing to do, appointments table already created. You can now use the microservice.")() );
        updateRequest =
            "CREATE TABLE appointments(id integer NOT NULL, customer_name VARCHAR(50) NOT NULL, " +
            "treatment_name VARCHAR(50) NOT NULL, treatment_id integer NOT NULL, " +
            "date VARCHAR(50) NOT NULL, start_time integer NOT NULL, end_time integer NOT NULL," +
            "PRIMARY KEY(id))";
        update@Database( updateRequest )( ret );
        println@Console("...successfully created appointments table. You can now use the microservice.")()
    }

}


main {

    [ createAppointment( request )( response ) {   
        scope ( insert ) {
            install ( SQLException => response.msg = "Could not save Appointment to Database" );

            if ( request.start_time >= request.end_time ) {
                response.msg = "Start time has to be before end time." 
            } else if ( request.start_time < 8 || request.start_time > 17 || request.end_time < 9 || request.end_time > 18) {
                response.msg = "Appointments are only available from 8 - 18. Please choose another timeslot." 
            } else {
                getTreatment@GETTREATMENT({.treatmentId = request.treatment_id})(treatment);
                if ( treatment.minduration > ( request.end_time - request.start_time ) ) {
                    response.msg = "An appointment for " + treatment.name + " takes at least " + treatment.minduration + " hour(s). Please choose another timeslot."
                } else if ( treatment.maxduration < (request.end_time - request.start_time)) {
                    response.msg = "An appointment for " + treatment.name + " takes maximum " + treatment.maxduration + " hour(s). Please choose another timeslot."
                } else {
                    conflicts = 0;
                    query@Database("select * from appointments")(queryResponse);
                    appointments -> queryResponse;

                    for( i = 0, i < #appointments.row, i++ ){
                        if ( treatment.name == appointments.row[i].treatment_name) {
                            if (appointments.row[i].date == request.date) {
                                if  ( !(request.start_time <= appointments.row[i].start_time && request.end_time <= appointments.row[i].start_time) && 
                                !(appointments.row[i].end_time <= request.end_time && appointments.row[i].end_time <= request.start_time) ) {
                                    conflicts += 1
                                }
                            }
                        }
                    }

                    if (conflicts == 0) {
                        updateRequest =
                            "INSERT INTO appointments(id, customer_name, treatment_name, treatment_id, date, start_time, end_time) " +
                            "VALUES (:id, :customer_name, :treatment_name, :treatment_id, :date, :start_time, :end_time)";
                        updateRequest.id = request.id;
                        updateRequest.customer_name = request.customer_name;
                        updateRequest.treatment_name = treatment.name;
                        updateRequest.treatment_id = request.treatment_id;
                        updateRequest.date = request.date;
                        updateRequest.start_time = request.start_time;
                        updateRequest.end_time = request.end_time;
                        update@Database( updateRequest )( result )
                        if (result == 1) {
                            response.msg = "Appointment successfully created!"
                            confirmAppointment@CONFIRMATION({
                                .id = request.id;
                                .customer_name = request.customer_name;
                                .treatment_name = treatment.name;
                                .start_time = request.start_time;
                                .end_time = request.end_time;
                                .date = request.date                                
                                })
                        } else {
                            response.msg = "Could not save appointment, please try again."
                        }
                        
                    } else {
                        response.msg = "There were " + conflicts + " conflicts with other appointments. Please choose a free timeslot."
                    }
                }
            }       
        }    
    }]


    [ getAppointment( request )( response ) {
        queryRequest =
        "SELECT * FROM appointments WHERE id=:id";
        queryRequest.id = request.appointmentId;
        query@Database( queryRequest )( queryResponse );

        response.id = queryResponse.row[0].id;
        response.customer_name = queryResponse.row[0].customer_name;
        response.treatment_id = queryResponse.row[0].treatment_id;
        response.date = queryResponse.row[0].date;
        response.start_time = queryResponse.row[0].start_time;
        response.end_time = queryResponse.row[0].end_time
    }]


    [ getAppointments( request )( response ) {
        query@Database("select * from appointments")(queryResponse);
        response.appointments -> queryResponse.row        
    }]

}


