package appointments.msimpl;

import appointments.msimpl.Appointment;
import io.micronaut.configuration.rabbitmq.annotation.Binding;
import io.micronaut.configuration.rabbitmq.annotation.RabbitClient;

@RabbitClient("beautysalon")
public interface ConfirmationClient {

	@Binding("newappointments")
	void sendConfirmation(Appointment appointment);
}