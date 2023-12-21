CREATE OR REPLACE VIEW AUCTION_DAY_AHEAD_VIEW AS
SELECT DAY_AHEAD_VIEW.*,
        CASE
            WHEN status = 'OPEN_CAPACITY' OR status = 'OPEN_ENERGY' THEN 1
            WHEN status = 'NEW_CAPACITY' OR status = 'NEW_ENERGY' THEN 2
            ELSE 3
        END AS status_code
FROM (
        SELECT
                ada.id,
                CASE
                    WHEN ${now_in_utc} < ada.auction_day THEN 'SCHEDULED'
                    WHEN auction_type = 'CAPACITY' AND ${now_in_utc} < ada.capacity_gate_opening_time THEN 'NEW_CAPACITY'
                    WHEN auction_type = 'CAPACITY' AND ${now_in_utc} >= ada.capacity_gate_opening_time AND ${now_in_utc} <= ada.capacity_gate_closure_time THEN 'OPEN_CAPACITY'
                    WHEN auction_type = 'CAPACITY' AND ${now_in_utc} > ada.capacity_gate_closure_time THEN 'CLOSED_CAPACITY'
                    WHEN auction_type = 'ENERGY' AND ${now_in_utc} < ada.energy_gate_opening_time THEN 'NEW_ENERGY'
                    WHEN auction_type = 'ENERGY' AND ${now_in_utc} >= ada.energy_gate_opening_time AND ${now_in_utc} <= ada.energy_gate_closure_time THEN 'OPEN_ENERGY'
                    WHEN auction_type = 'ENERGY' AND ${now_in_utc} > ada.energy_gate_closure_time THEN 'CLOSED_ENERGY'
                END AS status,
                ada.name,
                ada.auction_day,
                ada.delivery_date,
                ada.auction_type,
                prod.short_name AS product_name,
                prod.id AS product_id,
                prod.min_bid_size AS product_min_bid_size,
                prod.max_bid_size AS product_max_bid_size,
                ada.energy_gate_opening_time,
                ada.energy_gate_closure_time,
                ada.capacity_gate_opening_time,
                ada.capacity_gate_closure_time,
                ada.min_desired_capacity,
                ada.max_desired_capacity,
                ada.min_desired_energy,
                ada.max_desired_energy,
                ada.capacity_availability_from,
                ada.capacity_availability_to,
                ada.energy_availability_from,
                ada.energy_availability_to,
                ada.created_by,
                ada.created_date,
                ada.last_modified_by,
                ada.last_modified_date,
                ada.auctions_series_id
        FROM
                AUCTION_DAY_AHEAD ada
                INNER JOIN product prod ON prod.id = ada.product_id
    ) DAY_AHEAD_VIEW
