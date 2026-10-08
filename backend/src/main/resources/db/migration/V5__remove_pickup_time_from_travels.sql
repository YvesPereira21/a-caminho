-- Remove coluna redundante pickup_time da tabela travels
ALTER TABLE travels DROP COLUMN IF EXISTS pickup_time;
