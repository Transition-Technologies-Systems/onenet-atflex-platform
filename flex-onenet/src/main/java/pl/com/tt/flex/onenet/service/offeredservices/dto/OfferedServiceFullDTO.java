package pl.com.tt.flex.onenet.service.offeredservices.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfferedServiceFullDTO extends OfferedServiceDTO {

	private byte[] fileSchemaZip;
	private byte[] fileSchemaSampleZip;

}
