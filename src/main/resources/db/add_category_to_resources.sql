-- Migration: Add category column to resources table
-- Date: 2026-02-07
-- Description: Adds the category field to support resource categorization

-- Add category column (nullable first to allow existing data)
ALTER TABLE resources 
ADD COLUMN IF NOT EXISTS category VARCHAR(255);

-- Update existing rows with a default category
UPDATE resources 
SET category = 'Uncategorized' 
WHERE category IS NULL;

-- Make category NOT NULL after setting defaults
ALTER TABLE resources 
ALTER COLUMN category SET NOT NULL;

-- Optional: Add index for category filtering
CREATE INDEX IF NOT EXISTS idx_resources_category 
ON resources (category);

-- Optional: Add index for user_id + created_at ordering (performance optimization)
CREATE INDEX IF NOT EXISTS idx_resources_user_created_at 
ON resources (user_id, created_at DESC);
