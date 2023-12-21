package pl.com.tt.flex.flex.agno.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.flex.agno.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.flex.agno.web.resource.error.BadRequestAlertException;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static pl.com.tt.flex.flex.agno.validator.kdm_model.KdmModelTimestampFileValidator.sortedHourNumbers;
import static pl.com.tt.flex.flex.agno.web.resource.error.ErrorConstants.WRONG_KDM_MODEL_TIMESTAMP_NUMBER;
import static pl.com.tt.flex.flex.agno.web.resource.kdm_model.KdmModelResource.ENTITY_NAME;

@Slf4j
public class TimestampFileUtil {

    public static BufferedReader getFileBufferedReader(FileDTO fileDTO) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fileDTO.getBytesData());
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
    }

    public static KdmModelTimestampFileDTO prepareTimestampFileDto(String strKdmModelId, String strFileId, MultipartFile multipartFile, String timestamp) {
        Long kdmModelId = getLongValue(strKdmModelId);
        Long fileId = getLongValue(strFileId);
        checkIdValid(kdmModelId);
        checkTimestampValid(timestamp);
        KdmModelTimestampFileDTO timestampFileDTO = new KdmModelTimestampFileDTO();
        timestampFileDTO.setTimestamp(timestamp);
        timestampFileDTO.setKdmModelId(kdmModelId);
        if (Objects.nonNull(multipartFile)) {
            String filename = prepareFilename(multipartFile, timestampFileDTO.getTimestamp(), timestampFileDTO.getKdmModelId());
            timestampFileDTO.setFileDTO(FileDTOUtil.parseMultipartFile(multipartFile, filename));
        }
        if (Objects.isNull(fileId))
            return timestampFileDTO;
        timestampFileDTO.setId(fileId);
        return timestampFileDTO;
    }

    private static void checkIdValid(Long kdmModelId) {
        if (Objects.isNull(kdmModelId))
            throw new BadRequestAlertException("kdmModelId is not a number", ENTITY_NAME, "wrongid");
    }

    private static void checkTimestampValid(String timestamp) {
        if (!sortedHourNumbers.contains(timestamp))
            throw new BadRequestAlertException("timestamp is not valid", ENTITY_NAME, WRONG_KDM_MODEL_TIMESTAMP_NUMBER);
    }

    private static String prepareFilename(MultipartFile multipartFile, String timestamp, Long kdmModelId) {
        String fileName = multipartFile.getOriginalFilename();
        String fileExtension = "." + FileDTOUtil.getFileExtension(fileName).name().toLowerCase(); // .kdm
        String filenameWithoutExtension = StringUtils.removeEnd(fileName, fileExtension);
        String filenameFormat = "%s_%s_%s" + fileExtension;
        String resultFilename = String.format(filenameFormat, filenameWithoutExtension, kdmModelId, timestamp);
        log.debug("prepareFilename() Change original filename {} to {}", fileName, resultFilename);
        return resultFilename;
    }

    private static Long getLongValue(String strKdmModelId) {
        try {
            return Long.parseLong(strKdmModelId);
        } catch (Exception e) {
            return null;
        }
    }
}
