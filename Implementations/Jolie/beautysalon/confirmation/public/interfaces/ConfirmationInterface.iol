type ConfirmationMessage: void {
    .customer_name: string //< Customer Name
    .id: int //< ID of the Appointment
    .treatment_name: string //<  Name of the Treatment
    .date: string //<  Date
    .start_time: int //< Start Time
    .end_time: int //< End Time
}

/**! This Service is responsible for sending a Confirmation to the User.*/
interface ConfirmationInterface {
  OneWay:
    /**! Confirmas a newly creted Appointment*/
    confirmAppointment( ConfirmationMessage )
}