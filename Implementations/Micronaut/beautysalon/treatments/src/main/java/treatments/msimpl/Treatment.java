package treatments.msimpl;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Treatment {
	
	public Treatment() {
	}
	
	public Treatment(@NotNull Integer id, @NotBlank String name, @NotNull BigDecimal price, @NotNull Integer minduration, @NotNull Integer maxduration) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.minduration = minduration;
		this.maxduration = maxduration;
	}

	private String name;

	private Integer id;

	private BigDecimal price;
	
	private Integer minduration;

	private Integer maxduration;
	
    @Schema(description="Name of the Treatment")
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

    @Schema(description="ID of the Treatment")
	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}
	
    @Schema(description="Price of the Treatment")
	public BigDecimal getPrice() {
		return this.price;
	}
	
	public void setPrice(final BigDecimal price) {
		this.price = price;
	}
	
    @Schema(description="Minimum duration of the Treatment")
	public Integer getMinduration() {
		return minduration;
	}

	public void setMinduration(final Integer minduration) {
		this.minduration = minduration;
	}
	
    @Schema(description="Maximum duration of the Treatment")
	public Integer getMaxduration() {
		return maxduration;
	}

	public void setMaxduration(final Integer maxduration) {
		this.maxduration = maxduration;
	}

}
