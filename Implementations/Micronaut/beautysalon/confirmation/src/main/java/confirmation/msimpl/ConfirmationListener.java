package confirmation.msimpl;

import io.micronaut.configuration.rabbitmq.annotation.Queue;
import io.micronaut.configuration.rabbitmq.annotation.RabbitListener;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;

@RabbitListener
public class ConfirmationListener {
	
	private final ConfirmationService confirmationService;
	
	public ConfirmationListener(ConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

	@Queue("newappointments")
	public void sendConfirmation(Appointment appointment) {
		confirmationService.confirmAppointment(appointment);
	}

}
