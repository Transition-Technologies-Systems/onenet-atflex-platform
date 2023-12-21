package pl.com.tt.flex.server.service.importData.auctionOffer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;

public interface AuctionOfferImportService {

    AuctionOfferImportDataResult importCmvcData(MultipartFile file) throws ObjectValidationException;

    AuctionOfferImportDataResult importPbcmDanoData(MultipartFile file) throws ObjectValidationException;

    void sendNotificationAboutImportOffer(AuctionOfferImportDataResult result) throws JsonProcessingException;

    AuctionOfferImportDataResult importSetoData(MultipartFile multipartFile) throws IOException, ImportDataException;
}
