package pl.com.tt.flex.onenet.service.providedata.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProvideDataResponseDTO {

	private String onenetId;
	private String title;
	private String businessObjectId;
	private String description;

}
