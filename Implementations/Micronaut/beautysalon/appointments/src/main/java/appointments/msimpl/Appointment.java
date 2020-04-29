package beautysalon.appointments.msimpl;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "appointment")
public class Appointment {

	public Appointment() {
	}

	public Appointment(@NotNull String customerName, Date date,@NotNull int startTime, int endTime, @NotNull int treatmentId) {
		this.customerName = customerName;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.treatmentId = treatmentId;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name = "customerName")
	private String customerName;

	@Column(name = "date")
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "dd.MM.yyyy")
	private Date date = new Date();

	@Column(name = "startTime")
	private Integer startTime;
	
	@Column(name = "endTime")
	private Integer endTime;
	
	@Column(name = "duration")
	private Integer duration;
	
	@Column(name = "treatmentName")
	private String treatmentName;

	@Column(name = "treatmentId")
	private int treatmentId;
	
    @Schema(description="Name of the Customer")
	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(final String customerName) {
		this.customerName = customerName;
	}

	@Schema(description="ID of the Appointment, automatically generatied")
	public Integer getId() {
		return id;
	}
	
	public void setId(final Integer id) {
		this.id = id;
	}
	
	@Schema(description="Date of the Appointment")
	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	@Schema(description="Starttime of the Appointment")
	public Integer getStartTime() {
		return startTime;
	}

	public void setStartTime(final Integer startTime) {
		this.startTime = startTime;
	}
	
	@Schema(description="Endtime of the Appointment")
	public Integer getEndTime() {
		return endTime;
	}

	public void setEndTime(final Integer endTime) {
		this.endTime = endTime;
	}

    @Schema(description="Duration of the appointment, is calculated from Starttime and Endtime")
	public Integer getDuration() {
		this.setDuration();
		return this.duration;
	}
	
	public void setDuration() {
		this.duration = this.endTime - this.startTime;
	}
	
    @Schema(description="ID of the Treatment")
	public int getTreatmentId() {
		return treatmentId;
	}

	public void setTreatmentId(final int treatmentId) {
		this.treatmentId = treatmentId;
	}
	
    @Schema(description="Name of the Treatment, is determined by the Treatment ID")
	public String getTreatmentName() {
		return treatmentName;
	}

	public void setTreatmentName(final String treatmentName) {
		this.treatmentName = treatmentName;
	}

}
