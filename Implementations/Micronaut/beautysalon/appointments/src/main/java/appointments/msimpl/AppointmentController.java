package appointments.msimpl;

import appointments.msimpl.Appointment;
import appointments.msimpl.AppointmentRepository;
import appointments.msimpl.Treatment;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.validation.Validated;
import io.micronaut.tracing.annotation.ContinueSpan;
import io.micronaut.tracing.annotation.SpanTag;
import io.micronaut.http.hateos.JsonError;
import io.micronaut.tracing.annotation.ContinueSpan;
import io.micronaut.tracing.annotation.SpanTag;
import io.micronaut.discovery.exceptions.NoAvailableServiceException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.retry.annotation.Retryable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.net.URI;
import java.util.List;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

import java.lang.Exception;

@Validated
@Controller("/appointments")
public class AppointmentController {

	@Inject
	protected final AppointmentRepository appointmentRepository;

	@Inject
	protected final TreatmentClient treatmentClient;

	public AppointmentController(AppointmentRepository appointmentRepository, TreatmentClient treatmentClient) {
		this.appointmentRepository = appointmentRepository;
		this.treatmentClient = treatmentClient;
	}

	/**
	 * 
	 * @param the ID of a Appointment
	 * @return 200 Details for the requested Appointment
	 * @return 404 No Appointment for given ID
	 * @return 500 Connection Error to Database
	 */
	@ContinueSpan
	@Get("/{id}")
	@Operation(summary = "Returns an Appointment", description = "Returns all Information for the reqeusted Appointment")
	@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
	@ApiResponse(responseCode = "GeneralError", description = "No Appointment for the given ID")
	@Tag(name = "appointment")
	public HttpResponse<Appointment> getAppointment(
			@Parameter(description = "The ID of the desired Appointment") @SpanTag("show.id") int id) {
		try {
			Appointment appointment = appointmentRepository.findAppointmentById(id).orElse(null);

			if (appointment != null) {
				return HttpResponse.ok(appointment);
			} else {
				return notFound("No Appointment exists for the given ID.\n");
			}
		} catch (Exception e) {
			return serverError("Could not read Appointment.\n");
		}
	}

	/**
	 * 
	 * @return 200 List of all offered Appointments
	 * @return 404 No stored Appointments
	 * @return 500 Connection Error to Database
	 */
	@ContinueSpan
	@Get(value = "/list")
	@Retryable(attempts = "3", delay = "2s")
	@Operation(summary = "Shows all Appointments", description = "Shows a list of all settled Appointments")
	@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
	@ApiResponse(responseCode = "GeneralError", description = "No Appointments settled")
	@Tag(name = "appointments")
	public HttpResponse<List<Appointment>> getAppointments() {
		try {
			List<Appointment> appointments = appointmentRepository.findAllAppointments();

			if (appointments.size() == 0) {
				return notFound("Currently there are no Appointments are booked.\n");
			}

			return HttpResponse.ok(appointments);
		} catch (Exception e) {
			return serverError("Could not read Appointments.\n");
		}
		
	}

	/**
	 * 
	 * @param new Appointment that should be save
	 * @return 201 Detailed information for newly created Appointment
	 * @return 400 Appointment Details do not match the requirements
	 * @return 500 Connection Error 
	 */
	@ContinueSpan
	@Post("/")
	@Operation(summary = "Saves an Appointment", description = "Saves a new Appointment")
	@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
	@ApiResponse(responseCode = "GeneralError", description = "Could not save Appointment")
	@Tag(name = "appointment")
	public HttpResponse<Appointment> createAppointment(
			@Parameter(description = "The Appointment to save") @SpanTag("save.appointment") @Body @Valid Appointment appointment) {

		if (appointment.getEndTime() <= appointment.getStartTime()) {
			return conflict("Start time has to be before end time.\n");
		} else if (appointment.getStartTime() < 8 || appointment.getEndTime() < 9 || appointment.getStartTime() > 18 || appointment.getEndTime() > 18) {
			return conflict("Appointments are only available from 8 - 18. Please choose another timeslot.\n");
		}
		try {
			String authorizationValue = "Basic YWRtaW46YWRtaW4=";
			Treatment treatment = treatmentClient.getTreatment(authorizationValue, appointment.getTreatmentId());
			if (treatment.getMinduration() > appointment.getDuration()) {
				return conflict("An appointment for " + treatment.getName() + " takes at least "
						+ treatment.getMinduration() + " hour(s). Please choose another timeslot.\n");
			} else if (treatment.getMaxduration() < appointment.getDuration()) {
				return conflict("An appointment for " + treatment.getName() + " takes maximum "
						+ treatment.getMaxduration() + " hour(s). Please choose another timeslot.\n");
			}

			int conflicts = 0;
			List<Appointment> allAppointments = appointmentRepository.findAllAppointments();
			for (Appointment a : allAppointments) {
				if (treatment.getName().equals(a.getTreatmentName())) {
					if (appointment.getDate().equals(a.getDate())) {
						if (!(appointment.getStartTime() <= a.getStartTime() && appointment.getEndTime() <= a.getStartTime())
								&& !(a.getEndTime() <= appointment.getEndTime() && a.getEndTime() <= appointment.getStartTime())) {
							conflicts++;
						}
					}
				}
			}

			if (conflicts == 0) {
				Appointment newAppointment = appointmentRepository.save(appointment.getCustomerName(), appointment.getDate(),
						appointment.getStartTime(), appointment.getEndTime(), treatment.getName(), appointment.getTreatmentId());

				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
				String dateString = format.format(newAppointment.getDate());

				return HttpResponse.created(newAppointment)
						.headers(headers -> headers.location(location(newAppointment.getId())));
			} else {
				return conflict("There were " + conflicts
						+ " conflicts with other appointments. Please choose a free timeslot.\n");
			}
		} catch (NoAvailableServiceException e) {
			return serverError("Could not read Treatment.\n");
		} catch (Exception e) {
			return notFound("Could not create Appointment.\n");
		} 

	}

	private HttpResponse notFound(String message) {
		return HttpResponse.notFound(message);
	}

	private HttpResponse conflict(String message) {
		return HttpResponse.badRequest(message);
	}
	
	private HttpResponse serverError(String message) {
		return HttpResponse.serverError(message);
	}

	protected URI location(int id) {
		return URI.create("/appointments/" + id);
	}

	protected URI location(Appointment appointment) {
		return location(appointment.getId());
	}

}
