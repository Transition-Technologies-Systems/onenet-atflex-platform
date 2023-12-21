package pl.com.tt.flex.onenet.service.connector.mapper;

import static pl.com.tt.flex.onenet.util.StringUtil.getStringOrNull;

import java.util.Map;

import org.mapstruct.Mapper;

import pl.com.tt.flex.onenet.model.ProvideDataDTO;
import pl.com.tt.flex.onenet.service.providedata.dto.ProvideDataResponseDTO;

@Mapper(componentModel = "spring")
public interface ProvideDataDTOMapper {

	default ProvideDataDTO toPostDTO(String encodedFile, String title, String description, String filename, String dataOfferingId, String code) {
		ProvideDataDTO provideData = new ProvideDataDTO();
		provideData.setFile(encodedFile);
		provideData.setCode(code);
		provideData.setDataOfferingId(dataOfferingId);
		provideData.setDescription(description);
		provideData.setFilename(filename);
		provideData.setTitle(title);
		return provideData;
	}

	default ProvideDataResponseDTO toResponseDTO(Map<String, Object> map) {
		ProvideDataResponseDTO provideDataDTO = new ProvideDataResponseDTO();
		provideDataDTO.setOnenetId(String.valueOf(map.get("id")));
		provideDataDTO.setTitle(getStringOrNull(map.get("title")));
		provideDataDTO.setBusinessObjectId(String.valueOf(map.get("data_catalog_business_object_id")));
		provideDataDTO.setDescription(getStringOrNull(map.get("description")));
		return provideDataDTO;
	}

}
