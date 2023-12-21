CREATE OR REPLACE VIEW KPI_VIEW AS
    SELECT kpi.id,
           kpi.type,
           kpi.date_from,
           kpi.date_to,
           kpi.created_by,
           kpi.created_date,
           kpi.last_modified_by,
           kpi.last_modified_date,
           o.order_en as type_order_en,
           o.order_pl as type_order_pl
    FROM KPI kpi
    LEFT JOIN KPI_TYPES o ON o.type = kpi.type