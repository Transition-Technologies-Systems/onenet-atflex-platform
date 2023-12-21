package pl.com.tt.flex.onenet.service.offeredservices.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfferedServiceMinDTO implements Serializable {
	private Long id;
	private String onenetId;
	private String title;
	private String serviceCode;
}
