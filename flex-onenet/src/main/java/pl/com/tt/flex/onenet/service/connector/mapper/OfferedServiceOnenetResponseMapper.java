package pl.com.tt.flex.onenet.service.connector.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceFullDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static pl.com.tt.flex.onenet.util.StringUtil.getStringOrNull;

@Mapper(componentModel = "spring")
public interface OfferedServiceOnenetResponseMapper {

	/**
	 * Wyciąga dane oferowanej usługi z mapy zwróconej przez onenet z endpointu GET /offered-services/list
	 */
	default OfferedServiceFullDTO getOfferedServiceFullDTOFromApiResponseMap(Map<String, Object> map) throws IOException {
		OfferedServiceFullDTO offeredService = new OfferedServiceFullDTO();
		offeredService.setOnenetId(getStringOrNull(map.get("id")));
		offeredService.setTitle(getStringOrNull(map.get("title")));
		offeredService.setBusinessObjectId(String.valueOf(map.get("data_catalog_business_object_id")));
		offeredService.setBusinessObject(getStringOrNull(map.get("data_catalog_business_object_name")));
		offeredService.setServiceCode(getStringOrNull(map.get("data_catalog_service_code")));
		offeredService.setDescription(getStringOrNull(map.get("profile_description")));
		String encodedFileSchema = getStringOrNull(map.get("file_schema"));
		String fileSchemaName = getStringOrNull(map.get("file_schema_filename"));
		if (!StringUtils.isEmpty(encodedFileSchema) && !StringUtils.isEmpty(fileSchemaName)) {
			offeredService.setFileSchemaZip(decodeAndCompress(extractFileBytesFromDataURI(encodedFileSchema), fileSchemaName));
		}
		String encodedFileSchemaSample = getStringOrNull(map.get("file_schema_sample"));
		String fileSchemaSampleName = getStringOrNull(map.get("file_schema_sample_filename"));
		if (!StringUtils.isEmpty(encodedFileSchemaSample) && !StringUtils.isEmpty(fileSchemaSampleName)) {
			offeredService.setFileSchemaSampleZip(decodeAndCompress(extractFileBytesFromDataURI(encodedFileSchemaSample), fileSchemaSampleName));
		}
		return offeredService;
	}

	/**
	 * Zwraca bajty pliku zawarte w schemacie URI danych.
	 * Przykładowe URI: "data:text/plain;base64,eGQ="
	 */
	private String extractFileBytesFromDataURI(String dataURI) {
		return dataURI.split(",")[1];
	}

	/**
	 * Dekoduje plik zaszyfrowany w base64, następnie kompresuje go i zwraca plik zip
	 */
	private static byte[] decodeAndCompress(String base64EncodedData, String fileName) throws IOException {
		byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedData);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (ZipOutputStream zout = new ZipOutputStream(bout)) {
			ZipEntry zipEntry = new ZipEntry(fileName);
			zipEntry.setTime(0);    //Domyślnie automatycznie uzupełniany jest czas kompresji. Ustawienie na sztywno zapewnia, że funkcja wywołana dla tych samych danych zwróci zawsze te same bajty.
			zout.putNextEntry(zipEntry);
			zout.write(decodedBytes);
			zout.closeEntry();
		}
		return bout.toByteArray();
	}

}
