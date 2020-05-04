type Appointment: void {
    .customer_name: string //< Name of the Customer
    .id: int //< ID of the Appointment
    .treatment_id: int //< ID of the Treatment
    .date: string //< Date 
    .start_time: int //< Start Time
    .end_time: int //<  End Time
}

/**! Requests one Appointment */
type GetAppointmentRequest: void {
    .appointmentId: int //< the ID of an Appointment
}

/**! Requests all Appointments */
type GetAppointmentsRequest: void 

/**! Returns all Appointments */
type AppointmentsResponse: void{
   .appointments*: undefined
}

/**! Response for a newly created  Appointment */
type CreateAppointmentResponse: void{
   .msg*: undefined
}

/**! This Service is responsible for the management of appointments. */
interface AppointmentInterface {
  RequestResponse:
    /**! Returns all Information for the requested Appointment */
    getAppointment ( GetAppointmentRequest )( Appointment ),
    /**! Shows a list of all settled Appointments */
    getAppointments ( GetAppointmentsRequest )( AppointmentsResponse ),
    /**! Saves a new Appointment */
    createAppointment( Appointment )( CreateAppointmentResponse )
}