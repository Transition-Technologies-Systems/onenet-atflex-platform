CREATE OR REPLACE VIEW AUCTION_CMVC_VIEW AS
SELECT CMVC_VIEW.*,
       CASE
         WHEN status = 'OPEN' THEN 1
         WHEN status = 'NEW' THEN 2
         ELSE 3
       END AS status_code
FROM
    (
       SELECT cmvc.id ,
              CASE
                  WHEN ${now_in_utc} < cmvc.gate_opening_time THEN 'NEW'
                  WHEN ${now_in_utc} >= cmvc.gate_opening_time AND ${now_in_utc} <= cmvc.gate_closure_time THEN 'OPEN'
                  ELSE 'CLOSED'
              END AS status,
              cmvc.name ,
              cmvc.auction_type ,
              prod.short_name AS product_name ,
              prod.id AS product_id ,
              (SELECT listagg(lt.name || '(' || lt.type || ')', ',') WITHIN GROUP (ORDER BY lacmvc.auction_cmvc_id)
              FROM localization_type lt
              INNER JOIN localization_auction_cmvc lacmvc ON lt.id = lacmvc.localization_type_id
              where lacmvc.auction_cmvc_id = cmvc.id) AS localization ,
              cmvc.delivery_date_from ,
              cmvc.delivery_date_to ,
              cmvc.gate_opening_time ,
              cmvc.gate_closure_time ,
              cmvc.min_desired_power ,
              cmvc.max_desired_power ,
              cmvc.created_by ,
              cmvc.created_date ,
              cmvc.last_modified_by ,
              cmvc.last_modified_date
       FROM AUCTION_CMVC cmvc
       INNER JOIN product prod ON prod.id = cmvc.product_id
    ) CMVC_VIEW
