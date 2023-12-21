package pl.com.tt.flex.onenet.service.consumedata.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ConsumeDataViewDTO implements Serializable {
	private Long id;
	private String title;
	private String onenetId;
	private String businessObject;
	private String dataSupplier;
	// nazwa_uzytkownika (nazwa_firmy_uzytkownika_z_consume_data)
	private String dataSupplierFull;
	private String description;
}
