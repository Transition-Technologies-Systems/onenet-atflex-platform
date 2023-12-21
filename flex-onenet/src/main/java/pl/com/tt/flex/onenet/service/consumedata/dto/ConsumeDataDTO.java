package pl.com.tt.flex.onenet.service.consumedata.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ConsumeDataDTO implements Serializable {
	private Long id;
	private String title;
	private String onenetId;
	private String businessObjectId;
	private String dataSupplier;
	private String description;
}
