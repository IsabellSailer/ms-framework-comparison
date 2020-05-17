package appointments.msimpl;


import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Treatment {
	
	public Treatment() {}
	
	public Treatment(@NotNull String name) {
		this.name = name;
	}

	private Integer id;

	private String name;
	
	private Integer minduration;
	
	private Integer maxduration;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}
	
	public Integer getMinduration() {
		return minduration;
	}

	public void setMinduration(final Integer minduration) {
		this.minduration = minduration;
	}
	
	public Integer getMaxduration() {
		return maxduration;
	}

	public void setMaxduration(final Integer maxduration) {
		this.maxduration = maxduration;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

}
