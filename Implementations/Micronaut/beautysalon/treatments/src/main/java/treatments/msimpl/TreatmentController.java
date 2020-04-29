package beautysalon.treatments.msimpl;

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
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.core.version.annotation.Version;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import javax.validation.Valid;
import io.reactivex.Single;
import java.net.URI;
import java.util.List;

@Validated
@Controller("/treatments")
public class TreatmentController {

	protected final TreatmentRepository treatmentRepository;

	public TreatmentController(TreatmentRepository treatmentRepository) {
		this.treatmentRepository = treatmentRepository;
	}

	/**
	 * 
	 * @param the ID of a Treatment
	 * @return 200 Details for the requested Treatment
	 * @return 404 No Treatment for given ID
	 * @return 500 Connection Error to Database
	 */
	@ContinueSpan
	@Get("/{id}")
	@Secured("isAuthenticated()")
	@Operation(summary = "Returns a Treatment", description = "Returns all Information for the reqeusted Treatment")
	@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
	@ApiResponse(responseCode = "GeneralError", description = "No Treatment for the given ID")
	@Tag(name = "treatment")
	public HttpResponse<Treatment> getTreatment(
			@Parameter(description = "The ID of the desired Treatment") @SpanTag("findTreatmentById.id") int id) {

		Treatment treatment = treatmentRepository.findTreatmentById(id).orElse(null);
		if (null != treatment.getPrice()) {
			return HttpResponse.ok(treatment);
		} else {
			return notFound("No Treatment exists for the given ID.\n");
		}

	}

	/**
	 * 
	 * @return 200 List of all offered Treatments
	 * @return 404 No stored Treatments
	 */
	@ContinueSpan
	@Get(value = "/list")
	@Secured(SecurityRule.IS_ANONYMOUS)
	@Operation(summary = "Shows all Treatments", description = "Shows a list of all offered Treatments")
	@ApiResponse(content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")))
	@ApiResponse(responseCode = "GeneralError", description = "No Treatments available")
	@Tag(name = "treatments")
	public HttpResponse<String> getTreatments() {
		try {
			List<Treatment> treatments = treatmentRepository.findAllTreatments();

			if (treatments.size() == 0) {
				return notFound("Currently there are no Treatments offered.\n");
			}

			String formattedList = "Available Treatments:\n\n";
			for (Treatment t : treatments) {
				formattedList = formattedList.concat(t.getName() + "\nID: " + t.getId() + "\nPrice: " + t.getPrice()
						+ " Euro\nMinimum Duration: " + t.getMinduration() + " hour(s)\nMaximum Duration: "
						+ t.getMaxduration() + " hour(s)\n\n");
			}

			return HttpResponse.ok(formattedList);
		} catch (Exception e) {
			return serverError("Could not read Treatments.\n");
		}

	}

	/**
	 * 
	 * @param new Treatment that should be saved
	 * @return 201 Detailed information for newly created Treatment
	 */
	@Version("1")
	@ContinueSpan
	@Post("/")
	@Secured("isAuthenticated()")
	@Operation(summary = "Saves a Treatment", description = "Saves a new Treatment")
	@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
	@ApiResponse(responseCode = "GeneralError", description = "Could not save Treatment")
	@Tag(name = "treatment")
	public Single<HttpResponse<Treatment>> createTreatmentOne(
			@Parameter(description = "The Treatment to save") @Body @Valid Single<Treatment> treatment) {
		return treatment.map(newtreatment -> {
			treatmentRepository.save(newtreatment.getId(), newtreatment.getName(), newtreatment.getPrice(),
					newtreatment.getMinduration(), newtreatment.getMaxduration());
			return HttpResponse.created(newtreatment);
		});
	}

	/**
	 * 
	 * @param new Treatment that should be saved
	 * @return 201 Detailed information for newly created Treatment
	 */
	@Version("2")
	@ContinueSpan
	@Post("/")
	@Secured("isAuthenticated()")
	@Operation(summary = "Saves a Treatment", description = "Saves a new Treatment")
	@ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
	@ApiResponse(responseCode = "GeneralError", description = "Could not save Treatment")
	@Tag(name = "treatment")
	public HttpResponse<Treatment> createTreatmentTwo(
			@Parameter(description = "The Treatment to save") @Body @Valid Treatment treatment) {
		Treatment newtreatment = treatmentRepository.save(treatment.getId(), treatment.getName(), treatment.getPrice(),
				treatment.getMinduration(), treatment.getMaxduration());
		return HttpResponse.created(newtreatment).headers(headers -> headers.location(location(newtreatment.getId())));
	}

	private HttpResponse notFound(String message) {
		return HttpResponse.notFound(message);
	}

	private HttpResponse serverError(String message) {
		return HttpResponse.serverError(message);
	}

	protected URI location(int id) {
		return URI.create("/treatments/" + id);
	}

	protected URI location(Treatment treatment) {
		return location(treatment.getId());
	}

}
