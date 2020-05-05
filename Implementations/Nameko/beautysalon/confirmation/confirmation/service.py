from nameko.events import event_handler
from nameko_tracer import Tracer


class ConfirmationService:
    name = "confirmation"

    tracer = Tracer()

    @event_handler("appointments", "booked_appointment")
    def confirm_appointment(self, appointment_detail):
        """
        Confirms a new Appointment
        :param self:    
        :param appointment_detail: newly created Appointment
        """
        
        print("Thank you " + appointment_detail['customer_name'] + "!\nYour appointment for\n" + appointment_detail['treatment_name'] + "\nwill be on " + appointment_detail['date'] + "\nfrom " + str(appointment_detail['start_time']) + ":00h to " + str(appointment_detail['end_time']) + ":00h.")