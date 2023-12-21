package pl.com.tt.flex.server.service.importData.auctionOffer.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pl.com.tt.flex.model.service.dto.MinimalDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@ToString
public class AuctionOfferImportDataResult implements Serializable {

    private final List<Long> importedBids = new ArrayList<>();
    //MiminalDto<ID_BIDA, BŁAD). Id trzymamy jako String, w celu pokazania blednego wpisania ID importowanej oferty np. 12aa
    private final List<MinimalDTO<String, String>> notImportedBids = new ArrayList<>();

    public void addImportedBids(Long importedBid) {
        this.importedBids.add(importedBid);
    }

    public void addAllImportedBids(List<Long> importedBids) {
        this.getImportedBids().addAll(importedBids);
    }

    public void addNotImportedBids(MinimalDTO<String, String> notImportedBid) {
        this.notImportedBids.add(notImportedBid);
    }

    public void addAllNotImportedBids(List<MinimalDTO<String, String>> notImportedBids) {
        this.notImportedBids.addAll(notImportedBids);
    }
}
