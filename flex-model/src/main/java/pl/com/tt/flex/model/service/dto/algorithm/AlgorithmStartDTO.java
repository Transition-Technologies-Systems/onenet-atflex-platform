package pl.com.tt.flex.model.service.dto.algorithm;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AlgorithmStartDTO implements Serializable {

	@NotNull
	private Long kdmModelId;
	@NotNull
	private Instant deliveryDate;
	private List<Long> offers = new ArrayList<>();

}
