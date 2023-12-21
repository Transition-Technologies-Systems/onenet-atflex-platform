package pl.com.tt.flex.server.service.product.mapper;


import org.mapstruct.*;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.product.ProductFileEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Mapper for the entity {@link ProductEntity} and its DTO {@link ProductDTO}.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, ProductFileMapper.class})
public interface ProductMapper extends EntityMapper<ProductDTO, ProductEntity> {

    @Mapping(source = "psoUser.id", target = "psoUserId")
    @Mapping(source = "ssoUsers", target = "ssoUserIds", qualifiedByName = "ssoUsersToIds")
    @Mapping(source = "files", target = "filesMinimal", qualifiedByName = "filesToMinimal")
    @Mapping(source = "files", target = "files", ignore = true)
    ProductDTO toDto(ProductEntity productEntity);

    @Mapping(source = "psoUserId", target = "psoUser")
    @Mapping(source = "ssoUserIds", target = "ssoUsers")
    ProductEntity toEntity(ProductDTO productDTO);

    @Named("ssoUsersToIds")
    default List<Long> ssoUsersToIds(Set<UserEntity> users) {
        return users.stream().map(UserEntity::getId).collect(Collectors.toList());
    }

    @Named("filesToMinimal")
    default List<FileMinDTO> filesToMinimal(Set<ProductFileEntity> files) {
        return files.stream()
            .map(fileEntity -> new FileMinDTO(fileEntity.getId(), fileEntity.getFileName()))
            .sorted(Comparator.comparing(FileMinDTO::getFileId))
            .collect(Collectors.toList());
    }

    @AfterMapping
    default void linkFiles(@MappingTarget ProductEntity productEntity) {
        productEntity.getFiles().forEach(productFileEntity -> productFileEntity.setProduct(productEntity));
    }

    default ProductEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(id);
        return productEntity;
    }

    List<ProductMinDTO> toMinDto(List<ProductEntity> entityList);

    default ProductMinDTO toMinDto(ProductEntity entity) {
        if (nonNull(entity)) {
            ProductMinDTO productMinDTO = new ProductMinDTO();
            productMinDTO.setId(entity.getId());
            productMinDTO.setShortName(entity.getShortName());
            productMinDTO.setFullName(entity.getFullName());
            productMinDTO.setMinBidSize(entity.getMinBidSize());
            productMinDTO.setMaxBidSize(entity.getMaxBidSize());
            productMinDTO.setMaxFullActivationTime(entity.getMaxFullActivationTime());
            productMinDTO.setMinRequiredDeliveryDuration(entity.getMinRequiredDeliveryDuration());
            productMinDTO.setActive(entity.isActive());
            productMinDTO.setDirection(entity.getDirection());
            return productMinDTO;
        }
        return null;
    }
}
