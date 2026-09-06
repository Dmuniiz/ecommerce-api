CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_categories_name_trgm ON categories USING GIN (name gin_trgm_ops);
SET pg_trgm.similarity_threshold = 0.3;