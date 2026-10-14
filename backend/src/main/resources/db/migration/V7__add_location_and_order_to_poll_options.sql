-- V7: Adiciona coordenadas geográficas em poll_options e suporte a ordenação (order_point) de paradas em templates e enquetes

ALTER TABLE poll_options
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;

ALTER TABLE template_options
    ADD COLUMN order_point INTEGER DEFAULT 0;

ALTER TABLE poll_options_rel
    ADD COLUMN order_point INTEGER DEFAULT 0;

-- Atualiza dados mock existentes em Mamanguape
UPDATE poll_options SET latitude = -6.8375, longitude = -35.1278 WHERE option_id = '30000000-0000-0000-0000-000000000001';
UPDATE poll_options SET latitude = -6.8390, longitude = -35.1260 WHERE option_id = '30000000-0000-0000-0000-000000000002';
UPDATE poll_options SET latitude = -6.8350, longitude = -35.1290 WHERE option_id = '30000000-0000-0000-0000-000000000003';

UPDATE template_options SET order_point = 0 WHERE option_id = '30000000-0000-0000-0000-000000000001';
UPDATE template_options SET order_point = 1 WHERE option_id = '30000000-0000-0000-0000-000000000002';
UPDATE template_options SET order_point = 2 WHERE option_id = '30000000-0000-0000-0000-000000000003';

UPDATE poll_options_rel SET order_point = 0 WHERE option_id = '30000000-0000-0000-0000-000000000001';
UPDATE poll_options_rel SET order_point = 1 WHERE option_id = '30000000-0000-0000-0000-000000000002';
UPDATE poll_options_rel SET order_point = 2 WHERE option_id = '30000000-0000-0000-0000-000000000003';

