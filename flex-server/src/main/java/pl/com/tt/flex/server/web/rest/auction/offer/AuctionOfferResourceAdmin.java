package pl.com.tt.flex.server.web.rest.auction.offer;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_EXPORT;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_EXPORT_SETO;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_CMVC;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_PBCM_DANO;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_SETO;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_MANAGE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AUCTIONS_OFFER_VIEW;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;
import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.SETO_DETAIL_SHEET;
import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;
import static pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity_.ID;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.OFFER_EXPORT_DSO;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.OFFER_EXPORT_TSO;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.ADMIN_BIDS_EVALUATION;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_OFFERS_TEMPLATE_INCORRECT;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferService;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferViewQueryService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewCriteria;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.importData.auctionOffer.AuctionOfferImportService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDTO;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.importData.auctionOffer.mapper.AuctionOfferImportMapper;
import pl.com.tt.flex.server.service.mail.dto.NotificationResultDTO;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.auction.AuctionOfferViewValidator;
import pl.com.tt.flex.server.validator.auction.da.SetoOfferUpdateFileValidator;

/**
 * FLEX-ADMIN REST controller for managing {@link AuctionCmvcOfferEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auctions/offers")
public class AuctionOfferResourceAdmin extends AuctionOfferResource {

    private final AuctionOfferImportService auctionOfferImportService;
    private final AuctionOfferImportMapper auctionOfferImportMapper;
    private final UserService userService;
    private final AuctionOfferViewValidator auctionOfferViewValidator;
    private final SetoOfferUpdateFileValidator setoOfferUpdateFileValidator;

    public AuctionOfferResourceAdmin(final AuctionOfferService offerService, final AuctionOfferViewQueryService offerViewQueryService,
                                     final UserService userService, final FspService fspService, final AuctionOfferImportService auctionOfferImportService,
                                     final AuctionOfferImportMapper auctionOfferImportMapper, final AuctionOfferViewValidator auctionOfferViewValidator,
                                     final SetoOfferUpdateFileValidator setoOfferUpdateFileValidator) {
        super(offerService, offerViewQueryService, userService, fspService);
        this.auctionOfferImportService = auctionOfferImportService;
        this.userService = userService;
        this.auctionOfferImportMapper = auctionOfferImportMapper;
        this.auctionOfferViewValidator = auctionOfferViewValidator;
        this.setoOfferUpdateFileValidator = setoOfferUpdateFileValidator;
    }

    @GetMapping("/view")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_VIEW + "\")")
    public ResponseEntity<List<AuctionOfferViewDTO>> getAllViewOffers(AuctionOfferViewCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get AuctionOffers by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        Page<AuctionOfferViewDTO> page = offerViewQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PatchMapping("/view/mark")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_MANAGE + "\")")
    public ResponseEntity<Void> updateStatus(@RequestParam("status") AuctionOfferStatus status, @RequestParam("ids") List<Long> ids) throws ObjectValidationException {
        log.debug("{} - REST request to update status AuctionOffer : {}", FLEX_ADMIN_APP_NAME, status);
        auctionOfferViewValidator.validateStatusChange(ids, status);
        sendNotificationsUponAcceptingOffer(status, ids);
        offerService.updateStatus(status, ids);
        if(Set.of(AuctionOfferStatus.ACCEPTED, AuctionOfferStatus.REJECTED).contains(status)) {
            offerService.saveVolumeTransferredToBM(ids);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET  /admin/auctions/offers/get-products-used-in-offers} : get all products used in offers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the productNameMinDTO}.
     */
    @GetMapping("/get-products-used-in-offers")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_VIEW + "\")")
    public ResponseEntity<List<ProductNameMinDTO>> findAllProductsUsedInOffer() {
        log.debug("{} - REST request to get all products used in offers", FLEX_ADMIN_APP_NAME);
        List<ProductNameMinDTO> products = offerService.findAllProductsUsedInOffer();
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/view/export")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_EXPORT + "\")")
    public ResponseEntity<FileDTO> exportAll(AuctionOfferViewCriteria criteria) throws IOException, ObjectValidationException {
        Page<AuctionOfferViewDTO> offersDtoPage = getAuctionOffersToExport(criteria);
        return ResponseEntity.ok().body(offerService.exportOffersToFile(offersDtoPage.getContent(), userService.getLangKeyForCurrentLoggedUser(), ADMIN_BIDS_EVALUATION, STANDARD_DETAIL_SHEET));
    }

    @GetMapping("/view/export/seto")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_EXPORT_SETO + "\")")
    public ResponseEntity<FileDTO> exportAllSeto(AuctionOfferViewCriteria criteria) throws IOException, ObjectValidationException {
        Page<AuctionOfferViewDTO> offersDtoPage = getAuctionOffersToExportSeto(criteria);
        return ResponseEntity.ok().body(offerService.exportOffersToFile(offersDtoPage.getContent(), userService.getLangKeyForCurrentLoggedUser(), ADMIN_BIDS_EVALUATION, SETO_DETAIL_SHEET));
    }

    @GetMapping("/view/export/email")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_EXPORT + "\")")
    public ResponseEntity<NotificationResultDTO> exportAllToEmail(AuctionOfferViewCriteria criteria) throws IOException, ObjectValidationException {
        Page<AuctionOfferViewDTO> offersDtoPage = getAuctionOffersToExport(criteria);
        NotificationResultDTO notificationResultDTO = offerService.exportOffersToFileAndSendEmail(offersDtoPage.getContent(), userService.getLangKeyForCurrentLoggedUser(), ADMIN_BIDS_EVALUATION, STANDARD_DETAIL_SHEET, OFFER_EXPORT_TSO);
        return ResponseEntity.ok(notificationResultDTO);
    }

    @GetMapping("/view/export/email/seto")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_EXPORT_SETO + "\")")
    public ResponseEntity<NotificationResultDTO> exportAllSetoToEmail(AuctionOfferViewCriteria criteria) throws IOException, ObjectValidationException {
        Page<AuctionOfferViewDTO> offersDtoPage = getAuctionOffersToExportSeto(criteria);
        NotificationResultDTO notificationResultDTO = offerService.exportOffersToFileAndSendEmail(offersDtoPage.getContent(), userService.getLangKeyForCurrentLoggedUser(), ADMIN_BIDS_EVALUATION, SETO_DETAIL_SHEET, OFFER_EXPORT_DSO);
        return ResponseEntity.ok(notificationResultDTO);
    }

    @PostMapping("/view/import/cmvc")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_CMVC + "\")")
    public ResponseEntity<AuctionOfferImportDTO> importCmvcBids(@RequestPart("file") MultipartFile file) throws IOException, ImportDataException, ObjectValidationException {
        log.debug("REST request to import flex potential from cmvc file: {}", file.getName());
        auctionOfferViewValidator.validateOfferImportFileExtension(file);
        AuctionOfferImportDataResult resultDTO = auctionOfferImportService.importCmvcData(file);
        return sendNotificationAboutResultOfImport(resultDTO);
    }

    @PostMapping("/view/import/pbcm-dano")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_PBCM_DANO + "\")")
    public ResponseEntity<AuctionOfferImportDTO> importPbcmDanoBids(@RequestPart("file") MultipartFile file) throws IOException, ImportDataException, ObjectValidationException {
        log.debug("REST request to import flex potential from pbcm/dano file: {}", file.getName());
        auctionOfferViewValidator.validateOfferImportFileExtension(file);
        AuctionOfferImportDataResult resultDTO = auctionOfferImportService.importPbcmDanoData(file);
        return sendNotificationAboutResultOfImport(resultDTO);
    }

    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_SETO + "\")")
    @PostMapping("/view/import/seto")
    public ResponseEntity<AuctionOfferImportDTO> importSetoOfferUpdate(@RequestPart(value = "file") MultipartFile multipartFile) throws ObjectValidationException, IOException, ImportDataException {
        log.debug("FLEX_ADMIN - REST request to import day ahead offer updates from seto comparison file");
        setoOfferUpdateFileValidator.checkOfferUpdateFileValid(multipartFile);
        AuctionOfferImportDataResult resultDTO = auctionOfferImportService.importSetoData(multipartFile);
        return sendNotificationAboutResultOfImport(resultDTO);
    }

    private ResponseEntity<AuctionOfferImportDTO> sendNotificationAboutResultOfImport(AuctionOfferImportDataResult resultDTO) throws ObjectValidationException, JsonProcessingException {
        if (CollectionUtils.isEmpty(resultDTO.getImportedBids()) && CollectionUtils.isEmpty(resultDTO.getNotImportedBids())) {
            throw new ObjectValidationException("No offers to import", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
        auctionOfferImportService.sendNotificationAboutImportOffer(resultDTO);
        return ResponseEntity.ok(auctionOfferImportMapper.toDto(resultDTO));
    }

    private void sendNotificationsUponAcceptingOffer(AuctionOfferStatus status, List<Long> ids) {
        if (status.equals(AuctionOfferStatus.ACCEPTED)) {
            offerService.sendInformationAboutAcceptedCapacityOffer(ids);
            offerService.sendReminderAboutActivation(ids);
        }
    }

    public Page<AuctionOfferViewDTO> getAuctionOffersToExport(AuctionOfferViewCriteria criteria) {
        log.debug("REST request to export all offers");
        auctionOfferViewValidator.validateOfferExportCriteria(criteria);
        PageRequest pageRequest = PageRequest.of(0, (int) offerViewQueryService.countByCriteria(criteria), Sort.by(ID).descending());
        return offerViewQueryService.findByCriteria(criteria, pageRequest);
    }

    public Page<AuctionOfferViewDTO> getAuctionOffersToExportSeto(AuctionOfferViewCriteria criteria) {
        log.debug("REST request to export all offers to seto file");
        auctionOfferViewValidator.validateOfferExportCriteria(criteria);
        PageRequest pageRequest = PageRequest.of(0, (int) offerViewQueryService.countByCriteria(criteria), Sort.by(ID).descending());
        return offerViewQueryService.findByCriteria(criteria, pageRequest);
    }
}
