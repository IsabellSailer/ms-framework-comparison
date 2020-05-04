type GetAppointmentRequest:void {
  .appointmentId[1,1]:int
}

type Appointment:void {
  .date[1,1]:string
  .start_time[1,1]:int
  .end_time[1,1]:int
  .treatment_id[1,1]:int
  .customer_name[1,1]:string
  .id[1,1]:int
}

type GetAppointmentsRequest:void

type AppointmentsResponse:void {
  .appointments[0,*]:undefined
}

type CreateAppointmentResponse:void {
  .msg[0,*]:undefined
}

interface APPOINTMENTSInterface {
RequestResponse:
  getAppointment( GetAppointmentRequest )( Appointment ),
  getAppointments( GetAppointmentsRequest )( AppointmentsResponse ),
  createAppointment( Appointment )( CreateAppointmentResponse )
}



outputPort APPOINTMENTS {
  Protocol:http
  Location:"local"
  Interfaces:APPOINTMENTSInterface
}


embedded { Jolie: "appointment.ol" in APPOINTMENTS }
