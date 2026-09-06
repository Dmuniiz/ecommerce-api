-- V17__product_category_enhancements.sql
-- Database migration for Product and Category improvements
-- Date: 2026-07-06
-- Purpose: Add optimistic locking, timestamps, and indices

-- Add version column for optimistic locking
ALTER TABLE products ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

ALTER TABLE categories ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Add updated_at column for tracking changes
ALTER TABLE categories ADD COLUMN updated_at TIMESTAMP;
UPDATE categories SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE categories ALTER COLUMN updated_at SET NOT NULL;

-- ============================================================================
-- ADD INDICES FOR PERFORMANCE
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_product_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_product_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_product_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_category_name ON categories(name);

-- ============================================================================
-- VALIDATION CONSTRAINTS
-- ============================================================================

-- Ensure version is non-negative
ALTER TABLE products ADD CONSTRAINT ck_product_version_positive CHECK (version >= 0);
ALTER TABLE categories ADD CONSTRAINT ck_category_version_positive CHECK (version >= 0);

-- ============================================================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================================================

COMMENT ON COLUMN products.version IS 'Optimistic lock version number - managed by Hibernate';
COMMENT ON COLUMN categories.version IS 'Optimistic lock version number - managed by Hibernate';
COMMENT ON COLUMN categories.updated_at IS 'Last update timestamp for auditing';
