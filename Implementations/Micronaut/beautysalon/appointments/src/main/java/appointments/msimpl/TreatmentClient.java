package beautysalon.appointments.msimpl;

import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.annotation.Get;
import io.micronaut.retry.annotation.CircuitBreaker;
import io.micronaut.http.annotation.Header;

import java.util.List;

@Client(id = "treatments")
public interface TreatmentClient {

	@Get("/treatments/{treatmentId}")
	@CircuitBreaker(reset = "30s")
    Treatment getTreatment(@Header String authorization, int treatmentId);

}
