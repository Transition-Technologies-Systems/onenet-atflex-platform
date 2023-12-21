CREATE OR REPLACE VIEW SETTLEMENT_VIEW AS
    SELECT st.id,
           der.name AS der_name,
           der.fsp_id,
           st.offer_id,
           offer.auction_name,
           fsp.company_name,
           offer.company_name AS bsp_company_name,
           offer.accepted_delivery_period_from,
           offer.accepted_delivery_period_to,
           st.accepted_volume,
           st.activated_volume,
           st.settlement_amount,
           st.created_by,
           st.created_date,
           st.last_modified_by,
           st.last_modified_date,
           CASE offer.type
             WHEN 'CAPACITY' THEN 'kW'
             WHEN 'ENERGY' THEN 'kWh'
             ELSE NULL
           END AS unit,
           offer.created_by as offer_created_by,
           offer.status AS offer_status,
           offer.offer_category
    FROM SETTLEMENT st
    INNER JOIN UNIT der ON der.id = st.unit_id
    INNER JOIN AUCTION_OFFER_VIEW offer ON offer.id = st.offer_id
    INNER JOIN FSP fsp ON fsp.id = der.fsp_id
