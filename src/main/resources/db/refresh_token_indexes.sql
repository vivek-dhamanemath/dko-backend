-- Refresh Token Cleanup Indexes
-- Run these in Supabase SQL Editor for optimal cleanup performance

-- Index for expired token cleanup
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at
ON refresh_tokens (expires_at);

-- Composite index for revoked token cleanup
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked_created_at
ON refresh_tokens (revoked, created_at);

-- These indexes ensure cleanup queries run in O(log n) time instead of full table scans
