package pl.com.tt.flex.model.service.dto.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlgorithmPowerArgsDTO {
	private double activePower;
	private double pMin;
	private double pMax;
}
