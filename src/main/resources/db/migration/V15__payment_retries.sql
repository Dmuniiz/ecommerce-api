-- ============================================================================
-- Migration: Add Payment Retry System
-- Version: V15
-- Description: Creates payment_retries table for automatic retry of failed payments
--              with exponential backoff strategy
-- ============================================================================

-- Create payment_retries table
CREATE TABLE IF NOT EXISTS payment_retries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL UNIQUE REFERENCES payments(id) ON DELETE CASCADE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP,
    last_error_message TEXT,
    is_retryable BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_payment_retry_id
    ON payment_retries(payment_id);

CREATE INDEX IF NOT EXISTS idx_next_retry_at
    ON payment_retries(next_retry_at, is_retryable);

-- Add comment for documentation
COMMENT ON TABLE payment_retries IS
    'Tracks retry attempts for failed payments. Used by PaymentRetryScheduler to implement exponential backoff strategy.';

COMMENT ON COLUMN payment_retries.attempt_count IS
    'Number of retry attempts made so far';

COMMENT ON COLUMN payment_retries.max_attempts IS
    'Maximum number of retries allowed (configurable per deployment)';

COMMENT ON COLUMN payment_retries.next_retry_at IS
    'Timestamp when the next retry should be attempted (exponential backoff)';

COMMENT ON COLUMN payment_retries.is_retryable IS
    'Flag indicating if this payment should still be retried (false = permanently failed)';

-- ============================================================================
-- Rollback:
-- DROP TABLE IF EXISTS payment_retries CASCADE;
-- ============================================================================

