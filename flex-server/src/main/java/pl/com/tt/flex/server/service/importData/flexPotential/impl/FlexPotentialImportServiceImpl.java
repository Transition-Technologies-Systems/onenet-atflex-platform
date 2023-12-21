package pl.com.tt.flex.server.service.importData.flexPotential.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.dataimport.factory.DataImportFactory;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.importData.flexPotential.FlexPotentialImportService;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialImportDTO;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static pl.com.tt.flex.model.security.permission.Role.ROLE_FLEX_SERVICE_PROVIDER;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class FlexPotentialImportServiceImpl implements FlexPotentialImportService {

    private final DataImportFactory dataImportFactory;
    private final UnitService unitService;
    private final FlexPotentialService flexPotentialService;
    private final FspService fspService;
    private final ProductService productService;
    private final UserService userService;

    @Override
    public void importFlexPotential(MultipartFile file, String langKey) throws IOException, ImportDataException {
        List<FlexPotentialImportDTO> importedFlexPotential = dataImportFactory.getDataImport(FlexPotentialEntity.class, DataImportFormat.XLSX).doImport(file, Locale.forLanguageTag(langKey));
        List<FlexPotentialDTO> flexPotentialsToUpdate = getFlexPotentialsToUpdate(importedFlexPotential);
        flexPotentialsToUpdate.forEach(fp -> flexPotentialService.save(fp, null));
    }

    private List<FlexPotentialDTO> getFlexPotentialsToUpdate(List<FlexPotentialImportDTO> importedFlexPotential) throws ImportDataException {
        List<FlexPotentialDTO> flexPotentialEntities = new ArrayList<>();

        for (FlexPotentialImportDTO importedFlex : importedFlexPotential) {
            UserEntity currentLoggedUser = userService.getCurrentUser();
            if (currentLoggedUser.getRoles().contains(ROLE_FLEX_SERVICE_PROVIDER)) {
                checkIfUserHasPermissionToFspAndFlexPotential(importedFlex, currentLoggedUser);
            }
            UnitMinDTO unitDto = unitService.findByCodeAndFspCompanyName(importedFlex.getUnitCode(), importedFlex.getFspCompanyName()).orElseThrow(() -> new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_UNIT));
            FspDTO fspDTO = fspService.findByCompanyName(importedFlex.getFspCompanyName()).orElseThrow(() -> new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_FSP));
            ProductMinDTO productMinDTO = productService.findByShortName(importedFlex.getProductShortName()).orElseThrow(() -> new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_PRODUCT));
            FlexPotentialDTO updatedFlexPotential = getModifiedFlexPotential(importedFlex, unitDto, fspDTO, productMinDTO);
            flexPotentialEntities.add(updatedFlexPotential);
        }
        return flexPotentialEntities;
    }

    private FlexPotentialDTO getModifiedFlexPotential(FlexPotentialImportDTO importedFlex, UnitMinDTO unitDto, FspDTO fspDTO, ProductMinDTO productMinDTO) throws ImportDataException {
        FlexPotentialDTO flexPotentialDTO = flexPotentialService.findById(importedFlex.getId()).orElseThrow(() -> new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_FP));
        flexPotentialDTO.setFsp(fspDTO);
        flexPotentialDTO.setProduct(productMinDTO);
        flexPotentialDTO.setVolume(importedFlex.getVolume());
        flexPotentialDTO.setVolumeUnit(importedFlex.getVolumeUnit());
        flexPotentialDTO.setValidFrom(importedFlex.getValidFrom());
        flexPotentialDTO.setValidTo(importedFlex.getValidFrom());
        flexPotentialDTO.setActive(importedFlex.isActivated());
        flexPotentialDTO.setProductPrequalification(importedFlex.isProductPrequalification());
        flexPotentialDTO.setStaticGridPrequalification(importedFlex.isStaticGridPrequalification());
        flexPotentialDTO.setUnit(unitDto);
        return flexPotentialDTO;
    }

    private void checkIfUserHasPermissionToFspAndFlexPotential(FlexPotentialImportDTO importedFlex, UserEntity currentLoggedUser) throws ImportDataException {
        if (Objects.nonNull(currentLoggedUser.getFsp()) && !currentLoggedUser.getFsp().getCompanyName().equals(importedFlex.getFspCompanyName())) {
            throw new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_NO_PERMISSION_TO_FSP);
        }
        if (!flexPotentialService.isUserHasPermissionToFlexPotential(importedFlex.getId(), importedFlex.getFspCompanyName())) {
            throw new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_NO_PERMISSION_TO_FP);
        }
    }
}
