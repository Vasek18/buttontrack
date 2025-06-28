-- Update user_id column from integer to text for Google OAuth support
ALTER TABLE buttons ALTER COLUMN user_id TYPE TEXT;