type ConfirmationMessage: void {
    .customer_name: string
    .id: int
    .treatment_name: string
    .date: string
    .start_time: int
    .end_time: int
}


interface ConfirmationInterface {
  OneWay:
    confirmAppointment( ConfirmationMessage )
}