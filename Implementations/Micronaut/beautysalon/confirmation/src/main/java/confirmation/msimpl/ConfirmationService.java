package confirmation.msimpl;

import javax.inject.Singleton;

import confirmation.msimpl.Appointment;

@Singleton
public class ConfirmationService {

	public ConfirmationService() {
	}

	public void confirmAppointment(Appointment appointment) {

        System.out.format("Thank you %s! %nYour Appointment for %s was confirmed. %nIt will be on %d.%d.%d from %d:00 to %d:00.", appointment.getCustomerName(), appointment.getTreatmentName(), appointment.getDate().getDate(), (appointment.getDate().getMonth() + 1), (1900 + appointment.getDate().getYear()), appointment.getStartTime(), appointment.getEndTime());
	}
}
