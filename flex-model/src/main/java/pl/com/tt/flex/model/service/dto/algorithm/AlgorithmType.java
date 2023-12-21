package pl.com.tt.flex.model.service.dto.algorithm;

import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;

import java.io.Serializable;
import java.util.regex.Pattern;

import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType.CAPACITY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType.ENERGY;

public enum AlgorithmType implements Serializable {

    PBCM(CAPACITY, "^pbcm_input_pbcm_(\\w+)_(\\d{4}-\\d{2}-\\d{2})_(\\d+).xlsx$"),
    BM(ENERGY, "^pure_input_bm_(\\w+)_(\\d{4}-\\d{2}-\\d{2})_(\\d+).xlsx$"),
    DANO(ENERGY, "^dgia_input_dano_(\\w+)_(\\d{4}-\\d{2}-\\d{2})_(\\d+).xlsx$"),
    DISAGGREGATION(ENERGY, "^powers_(-?\\d+.\\d+?)_input_bm_(\\w+)_(\\d{4}-\\d{2}-\\d{2})_(\\d+).xlsx$");

    private final AuctionOfferType offerType;
    private final Pattern fileNamePattern;

    AlgorithmType(AuctionOfferType offerType, String fileNamePattern) {
        this.offerType = offerType;
        this.fileNamePattern = Pattern.compile(fileNamePattern);
    }

    public AuctionOfferType getOfferType() {
        return offerType;
    }

    public Pattern getFileNamePattern() {
        return fileNamePattern;}
}