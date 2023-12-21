package pl.com.tt.flex.server.service.product;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.product.ProductFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.dto.ProductMailDTO;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing {@link ProductEntity}.
 */
public interface ProductService extends AbstractService<ProductEntity, ProductDTO, Long> {

    Optional<ProductDTO> findOneWithUsers(Long productId);

    ProductDTO save(ProductEntity toEntity, List<Long> filesToRemove);

    Optional<ProductFileEntity> getProductFileByFileId(Long fileId);

    List<FileDTO> getZipWithAllFilesOfProduct(Long productId);

    void registerNewNotificationForProductCreation(ProductDTO productDTO);

    void registerNewNotificationForProductEdition(ProductDTO productDTO, ProductDTO oldProductDTO);

    void sendMailInformingAboutProductCreation(ProductEntity productEntity, List<MinimalDTO<Long, String>> usersToBeNotified, String psoUser, String ssoUsers);

    void sendMailInformingAboutProductEdition(ProductMailDTO oldProductMailDTO, ProductMailDTO productMailDTO, List<MinimalDTO<Long, String>> usersToBeNotified, Map<NotificationParam, NotificationParamValue> notificationParams);

    boolean isPsoUserOfAnyProduct(String login);

    FileDTO exportProductsToFile(List<ProductDTO> productToExport, String langKey, boolean isOnlyVisibleColumn, Screen screen) throws IOException;

    Optional<ProductMinDTO> findByShortName(String shortName);

	void deactivateProductsByValidFromToDates();

    void activateProductsByValidFromToDates();

    boolean existsByShortName(String shortName);

    boolean existsByShortNameAndIdNot(String shortName, Long id);

    boolean existsByFullNameAndIdNot(String fullName, Long id);

    boolean existsByFullName(String fullName);

	ProductNameMinDTO getProductShortName(Long id);

    Long findByFullNameOrShortName(String productName);

    Long findByFullName(String productName);
}
