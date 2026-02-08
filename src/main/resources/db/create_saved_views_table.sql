-- Migration: Create saved_views table
-- Date: 2026-02-07
-- Description: Adds table for storing user filter views

CREATE TABLE IF NOT EXISTS saved_views (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    filters TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_user_view_name UNIQUE(user_id, name)
);

-- Index for fast user lookups
CREATE INDEX IF NOT EXISTS idx_saved_views_user_id 
ON saved_views(user_id);

-- Index for user + created_at ordering
CREATE INDEX IF NOT EXISTS idx_saved_views_user_created 
ON saved_views(user_id, created_at DESC);
