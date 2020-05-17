package confirmation.msimpl;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import javax.inject.Singleton;

@Singleton
public class Appointment {

	public Appointment() {
	}
	
	@JsonFormat(pattern = "dd.MM.yyyy")
	private Date date = new Date();
	private String customerName;
	private String treatmentName;
	private Integer startTime;
	private Integer endTime;

	public Integer getStartTime() {
		return startTime;
	}

	public void setStartTime(final Integer startTime) {
		this.startTime = startTime;
	}
	
	public Integer getEndTime() {
		return endTime;
	}

	public void setEndTime(final Integer endTime) {
		this.endTime = endTime;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(final String customerName) {
		this.customerName = customerName;
	}
	
	public String getTreatmentName() {
		return treatmentName;
	}

	public void setTreatmentName(final String treatmentName) {
		this.treatmentName = treatmentName;
	}
}
