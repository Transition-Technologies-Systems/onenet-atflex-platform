package pl.com.tt.flex.server.service.product.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.server.domain.product.ProductFileEntity;
import pl.com.tt.flex.server.service.mapper.FileEntityMapper;
import pl.com.tt.flex.server.service.product.dto.ProductFileDTO;

/**
 * Mapper for the entity {@link ProductFileEntity} and its DTO {@link ProductFileDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface ProductFileMapper extends FileEntityMapper<ProductFileDTO, ProductFileEntity> {

    @Mapping(source = "product.id", target = "productId")
    ProductFileDTO toDto(ProductFileEntity productFileEntity);

    @Mapping(source = "productId", target = "product")
    @Mapping(source = "fileDTO", target = "fileExtension", qualifiedByName = "fileExtensionDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileName", qualifiedByName = "fileNameDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileZipData", qualifiedByName = "fileBase64DataDTOToEntity")
    ProductFileEntity toEntity(ProductFileDTO productFileDTO);
}
