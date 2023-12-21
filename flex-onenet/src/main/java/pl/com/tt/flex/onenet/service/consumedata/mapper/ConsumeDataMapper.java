package pl.com.tt.flex.onenet.service.consumedata.mapper;

import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataEntity;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataDTO;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Mapper(componentModel = "spring")
public interface ConsumeDataMapper extends EntityMapper<ConsumeDataDTO, ConsumeDataEntity> {
	ConsumeDataDTO toDto(ConsumeDataEntity consumeDataEntity);

	default ConsumeDataEntity toEntity(Map<String, Object> map) {
		ConsumeDataEntity consumeDataEntity = new ConsumeDataEntity();
		consumeDataEntity.setOnenetId(String.valueOf(map.get("id")));
		consumeDataEntity.setTitle(String.valueOf(map.get("data_title")));
		// z API jest otrzymywany tylko ID obiektu także trzeba dociągnąć dodatkowo nazwę obiektu by wyświetlić na froncie
		consumeDataEntity.setBusinessObjectId(String.valueOf(map.get("data_catalog_business_object_id")));
		consumeDataEntity.setDescription(String.valueOf(map.get("data_description")));
		// po ustaleniach z 15.02 na froncie ma być wyświetlana nazwa użytkownika oraz nazwa firmy dostawcy
		// Format: nazwa_uzytkownika (nazwa_firmy)
		consumeDataEntity.setDataSupplier(String.valueOf(map.get("provider_username")));
		consumeDataEntity.setDataSupplierCompanyName(String.valueOf(map.get("provider_company_name")));

		return consumeDataEntity;
	}

	default ConsumeDataEntity toEntity(MultipartFile multipartFile, String title, String description, String filename, String onenetId, String username,
                                       String businessObjectId) throws IOException {
		ConsumeDataEntity entity = new ConsumeDataEntity();
		entity.setDataSupplier(username);
		entity.setFileZip(bytesToZip(multipartFile.getBytes(), filename));
		entity.setOnenetId(onenetId);
		entity.setTitle(title);
		entity.setDescription(description);
		entity.setBusinessObjectId(businessObjectId);
		return entity;
	}

	private byte[] bytesToZip(byte[] bytes, String filename) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (ZipOutputStream zout = new ZipOutputStream(bout)) {
			ZipEntry zipEntry = new ZipEntry(filename);
			zout.putNextEntry(zipEntry);
			zout.write(bytes);
			zout.closeEntry();
		}
		return bout.toByteArray();
	}

}
