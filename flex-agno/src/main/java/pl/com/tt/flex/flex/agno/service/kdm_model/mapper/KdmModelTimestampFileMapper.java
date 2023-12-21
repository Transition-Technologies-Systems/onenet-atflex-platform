package pl.com.tt.flex.flex.agno.service.kdm_model.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelTimestampFileEntity;
import pl.com.tt.flex.flex.agno.service.kdm_model.util.KdmModelUtils;
import pl.com.tt.flex.flex.agno.service.mapper.FileEntityMapper;
import pl.com.tt.flex.flex.agno.util.ZipUtil;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampsMinimalDTO;

import java.util.List;

/**
 * Mapper for the entity {@link KdmModelTimestampFileEntity} and its DTO {@link KdmModelTimestampFileDTO}.
 */
@Mapper(componentModel = "spring", uses = {KdmModelMapper.class})
public interface KdmModelTimestampFileMapper extends FileEntityMapper<KdmModelTimestampFileDTO, KdmModelTimestampFileEntity> {

    @Mapping(source = "kdmModel.id", target = "kdmModelId")
    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = "fileName", target = "fileName")
    @Mapping(source = "id", target = "id")
    KdmModelTimestampFileDTO toDto(KdmModelTimestampFileEntity kdmModelTimestampFileEntity);

    @Mapping(source = "timestamp", target = "timestamp")
    @Mapping(source = ".", target = "stations", qualifiedByName = "getStationList")
    KdmModelTimestampsMinimalDTO toMinimalDto(KdmModelTimestampFileEntity kdmModelTimestampFileEntity);

    @Mapping(source = "kdmModelId", target = "kdmModel")
    @Mapping(source = "fileDTO", target = "fileExtension", qualifiedByName = "fileExtensionDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileName", qualifiedByName = "fileNameDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileZipData", qualifiedByName = "fileBase64DataDTOToEntity")
    KdmModelTimestampFileEntity toEntity(KdmModelTimestampFileDTO kdmModelTimestampFileDTO);

    @Named("getStationList")
    default List<String> getStationList(KdmModelTimestampFileEntity entity) {
        byte[] fileZipData = entity.getFileZipData();
        FileDTO fileDTO = ZipUtil.zipToFiles(fileZipData).get(0);
        return KdmModelUtils.getPowerStationsFromKdm(fileDTO);
    }
}
