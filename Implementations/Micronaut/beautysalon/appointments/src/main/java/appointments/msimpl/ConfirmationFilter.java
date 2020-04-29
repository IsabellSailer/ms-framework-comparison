package beautysalon.appointments.msimpl;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import java.util.Optional;

@Filter("/appointments/")
public class ConfirmationFilter implements HttpServerFilter {

	private final ConfirmationClient confirmationClient;

	public ConfirmationFilter(ConfirmationClient confirmationClient) {
		this.confirmationClient = confirmationClient;
	}

	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
		return Flowable.fromPublisher(chain.proceed(request)).flatMap(response -> Flowable.fromCallable(() -> {
			Optional<Appointment> appointment = response.getBody(Appointment.class);
			appointment.ifPresent(confirmationClient::sendConfirmation);

			return response;
		}));
	}
}
