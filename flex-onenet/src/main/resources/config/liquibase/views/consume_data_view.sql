CREATE OR REPLACE VIEW CONSUME_DATA_VIEW AS
    SELECT DISTINCT csm.id,
        csm.title,
        csm.onenet_id,
        os.business_object,
        csm.data_supplier,
        csm.data_supplier || ' (' || csm.data_supplier_company_name || ')' AS data_supplier_full,
        csm.description,
        csm.created_by,
        csm.created_date,
        csm.last_modified_by,
        csm.last_modified_date,
        (CASE
            WHEN csm.file_zip IS NOT NULL THEN 1
            ELSE 0
        END) as file_available

    FROM CONSUME_DATA csm
    LEFT JOIN OFFERED_SERVICES os ON csm.business_object_id = os.business_object_id