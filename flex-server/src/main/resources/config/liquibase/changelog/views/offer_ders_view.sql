CREATE OR REPLACE VIEW OFFER_DERS_VIEW AS
    SELECT offer.id as offer_id,
           fpu.unit_id as der_id
    FROM AUCTION_CMVC_OFFER offer
    INNER JOIN FLEX_POTENTIAL_UNITS fpu ON fpu.flex_potential_id = offer.flex_potential_id
    UNION
    SELECT offer.id as offer_id,
           der.id as der_id
    FROM AUCTION_DA_OFFER offer
    INNER JOIN UNIT der ON der.scheduling_unit_id = offer.scheduling_unit_id
