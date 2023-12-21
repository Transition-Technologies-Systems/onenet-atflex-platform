CREATE OR REPLACE VIEW AUCTION_OFFER_VIEW AS
   SELECT offer.id,
          cmvc.id AS auction_id,
          cmvc.name AS auction_name,
          offer.status,
          (SELECT cmvc_view.status FROM AUCTION_CMVC_VIEW cmvc_view WHERE cmvc_view.id = offer.auction_cmvc_id) AS auction_status,
          prod.id AS product_id,
          prod.short_name AS product_name,
          fsp.company_name,
          (SELECT listagg(u.name, ', ') FROM UNIT u, FLEX_POTENTIAL_UNITS fu WHERE u.Id = fu.unit_id AND fu.flex_potential_id = offer.flex_potential_id) AS ders,
          (SELECT listagg(l.name, ', ') FROM UNIT u, FLEX_POTENTIAL_UNITS fu, UNIT_COUP_POINT_ID_TYPE cp, LOCALIZATION_TYPE l WHERE offer.flex_potential_id = fu.flex_potential_id AND fu.unit_id = u.id AND u.id = cp.unit_id AND cp.localization_type_id = l.id) AS coupling_point,
          (SELECT listagg(l.name, ', ') FROM UNIT u, FLEX_POTENTIAL_UNITS fu, UNIT_POWER_STATION_TYPE ps, LOCALIZATION_TYPE l WHERE offer.flex_potential_id = fu.flex_potential_id AND fu.unit_id = u.id AND u.id = ps.unit_id AND ps.localization_type_id = l.id) AS power_station,
          (SELECT listagg(l.name, ', ') FROM UNIT u, FLEX_POTENTIAL_UNITS fu, UNIT_POINT_OF_CONNECTION_TYPE poc, LOCALIZATION_TYPE l WHERE offer.flex_potential_id = fu.flex_potential_id AND fu.unit_id = u.id AND u.id = poc.unit_id AND poc.localization_type_id = l.id) AS poc_with_lv,
          fsp.role AS role,
          fsp.id AS fsp_id,
          'CMVC' AS offer_category,
          'CMVC' || '_' || offer.type AS auction_category_and_type,
          offer.type,
          offer.price AS price,
          to_char(offer.volume, 'FM9999999999999999990D90') AS volume,
          0 AS volume_tooltip_visible,
          offer.volume_divisibility,
          to_char(offer.accepted_volume, 'FM9999999999999999990D90') AS accepted_volume,
          0 AS accepted_volume_tooltip_visible,
          offer.delivery_period_from,
          offer.delivery_period_to,
          offer.delivery_period_divisibility,
          offer.accepted_delivery_period_from,
          offer.accepted_delivery_period_to,
          offer.created_by,
          offer.created_date,
          offer.last_modified_by,
          offer.last_modified_date,
          0 as verified_volumes_percent,
          to_char(fp.id) as scheduling_unit_or_potential,
          fp.volume as flex_potential_volume,
          fp.volume_unit as flex_potential_volume_unit
   FROM AUCTION_CMVC_OFFER offer
   INNER JOIN AUCTION_CMVC cmvc ON cmvc.id = offer.auction_cmvc_id
   INNER JOIN PRODUCT prod ON prod.id = cmvc.product_id
   INNER JOIN FLEX_POTENTIAL fp ON fp.id = offer.flex_potential_id
   INNER JOIN FSP fsp ON fsp.id = fp.fsp_id
   UNION
   SELECT offer.id,
          da.id AS auction_id,
          da.name AS auction_name,
          offer.status,
          (SELECT da_view.status FROM AUCTION_DAY_AHEAD_VIEW da_view WHERE da_view.id = offer.auction_day_ahead_id) AS auction_status,
          prod.id AS product_id,
          prod.short_name AS product_name,
          fsp.company_name,
          ' ' AS ders,
          ' ' AS coupling_point,
          ' ' AS power_station,
          ' ' AS poc_with_lv,
          fsp.role AS role,
          fsp.id AS fsp_id,
          'DAY_AHEAD' AS offer_category,
          'DAY_AHEAD' || '_' || offer.type AS auction_category_and_type,
          offer.type,
          offer.price AS price,
          CASE WHEN offer.volume_from = 0 THEN to_char(offer.volume_to, 'FM9999999999999999990D90')
               WHEN offer.volume_to = 0 THEN to_char(offer.volume_from, 'FM9999999999999999990D90')
               ELSE to_char(offer.volume_to, 'FM9999999999999999990D90') || ' / ' || to_char(offer.volume_from, 'FM9999999999999999990D90')
          END AS volume,
          CASE WHEN offer.volume_from = 0 OR offer.volume_to = 0 THEN 0
               ELSE 1
          END AS volume_tooltip_visible,
          offer.volume_divisibility,
          CASE WHEN offer.accepted_volume_from = 0 AND (offer.volume_from = 0 OR offer.volume_to = 0) THEN to_char(offer.accepted_volume_to, 'FM9999999999999999990D90')
               WHEN offer.accepted_volume_to = 0 AND (offer.volume_from = 0 OR offer.volume_to = 0) THEN to_char(offer.accepted_volume_from, 'FM9999999999999999990D90')
               ELSE to_char(offer.accepted_volume_to, 'FM9999999999999999990D90') || ' / ' || to_char(offer.accepted_volume_from, 'FM9999999999999999990D90')
          END AS accepted_volume,
          CASE WHEN (offer.accepted_volume_from = 0 OR offer.accepted_volume_to = 0) AND (offer.volume_from = 0 OR offer.volume_to = 0) THEN 0
               ELSE 1
          END AS accepted_volume_tooltip_visible,
          offer.delivery_period_from,
          offer.delivery_period_to,
          offer.delivery_period_divisibility,
          offer.accepted_delivery_period_from,
          offer.accepted_delivery_period_to,
          offer.created_by,
          offer.created_date,
          offer.last_modified_by,
          offer.last_modified_date,
          offer.verified_volumes_percent,
          su.name as scheduling_unit_or_potential,
          null as flex_potential_volume,
          null as flex_potential_volume_unit
   FROM AUCTION_DA_OFFER offer
   INNER JOIN AUCTION_DAY_AHEAD da ON da.id = offer.auction_day_ahead_id
   INNER JOIN PRODUCT prod ON prod.id = da.product_id
   INNER JOIN SCHEDULING_UNIT su ON su.id = offer.scheduling_unit_id
   INNER JOIN FSP fsp ON fsp.id = su.bsp_id
   WHERE offer.type = 'CAPACITY'
   UNION
   SELECT offer.id,
          da.id AS auction_id,
          da.name AS auction_name,
          offer.status,
          (SELECT da_view.status FROM AUCTION_DAY_AHEAD_VIEW da_view WHERE da_view.id = offer.auction_day_ahead_id) AS auction_status,
          prod.id AS product_id,
          prod.short_name AS product_name,
          fsp.company_name,
          ' ' AS ders,
          ' ' AS coupling_point,
          ' ' AS power_station,
          ' ' AS poc_with_lv,
          fsp.role AS role,
          fsp.id AS fsp_id,
          'DAY_AHEAD' AS offer_category,
          'DAY_AHEAD' || '_' || offer.type AS auction_category_and_type,
          offer.type,
          offer.price AS price,
          CASE WHEN offer.volume_from = 0 THEN to_char(offer.volume_to, 'FM9999999999999999990D90')
               WHEN offer.volume_to = 0 THEN to_char(offer.volume_from, 'FM9999999999999999990D90')
               ELSE to_char(offer.volume_to, 'FM9999999999999999990D90') || ' / ' || to_char(offer.volume_from, 'FM9999999999999999990D90')
          END AS volume,
          CASE WHEN offer.volume_from = 0 OR offer.volume_to = 0 THEN 0
               ELSE 1
          END AS volume_tooltip_visible,
          offer.volume_divisibility,
          CASE WHEN offer.accepted_volume_from = 0 AND (offer.volume_from = 0 OR offer.volume_to = 0) THEN to_char(offer.accepted_volume_to, 'FM9999999999999999990D90')
               WHEN offer.accepted_volume_to = 0 AND (offer.volume_from = 0 OR offer.volume_to = 0) THEN to_char(offer.accepted_volume_from, 'FM9999999999999999990D90')
               ELSE to_char(offer.accepted_volume_to, 'FM9999999999999999990D90') || ' / ' || to_char(offer.accepted_volume_from, 'FM9999999999999999990D90')
          END AS accepted_volume,
          CASE WHEN (offer.accepted_volume_from = 0 OR offer.accepted_volume_to = 0)  AND (offer.volume_from = 0 OR offer.volume_to = 0) THEN 0
               ELSE 1
          END AS accepted_volume_tooltip_visible,
          offer.delivery_period_from,
          offer.delivery_period_to,
          offer.delivery_period_divisibility,
          offer.accepted_delivery_period_from,
          offer.accepted_delivery_period_to,
          offer.created_by,
          offer.created_date,
          offer.last_modified_by,
          offer.last_modified_date,
          offer.verified_volumes_percent,
          su.name as scheduling_unit_or_potential,
          null as flex_potential_volume,
          null as flex_potential_volume_unit
   FROM AUCTION_DA_OFFER offer
   INNER JOIN AUCTION_DAY_AHEAD da ON da.id = offer.auction_day_ahead_id
   INNER JOIN PRODUCT prod ON prod.id = da.product_id
   INNER JOIN SCHEDULING_UNIT su ON su.id = offer.scheduling_unit_id
   INNER JOIN FSP fsp ON fsp.id = su.bsp_id
   WHERE offer.type = 'ENERGY'

