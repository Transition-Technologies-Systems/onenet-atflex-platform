package pl.com.tt.flex.onenet.service.offeredservices.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfferedServiceDTO extends OfferedServiceMinDTO {
	private String businessObjectId;
	private String businessObject;
	private String description;
}
