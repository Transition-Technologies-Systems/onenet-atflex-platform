package pl.com.tt.flex.onenet.service.providedata.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProvideDataViewDTO implements Serializable {
	private Long id;
	private String title;
	private String onenetId;
	private String businessObject;
	private String description;
	private boolean fileAvailable;
}
