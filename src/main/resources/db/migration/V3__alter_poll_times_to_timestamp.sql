-- V3: Alteração de start_time e end_time de TIME para TIMESTAMP em polls
-- Permite que enquetes abram em um dia e fechem no outro (ex: 18:00 de hoje até 06:00 de amanhã)

ALTER TABLE polls
    ALTER COLUMN start_time TYPE TIMESTAMP WITHOUT TIME ZONE
        USING CASE 
            WHEN start_time IS NOT NULL AND poll_date IS NOT NULL 
                THEN (poll_date + start_time)::timestamp 
            ELSE NULL 
        END,
    ALTER COLUMN end_time TYPE TIMESTAMP WITHOUT TIME ZONE
        USING CASE 
            WHEN end_time IS NOT NULL AND poll_date IS NOT NULL 
                THEN (poll_date + end_time)::timestamp 
            ELSE NULL 
        END;
