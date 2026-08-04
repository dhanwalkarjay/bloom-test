-- Add missing columns to addresses table
ALTER TABLE public.addresses ADD COLUMN IF NOT EXISTS label TEXT;
ALTER TABLE public.addresses ADD COLUMN IF NOT EXISTS recipient_name TEXT;
ALTER TABLE public.addresses ADD COLUMN IF NOT EXISTS recipient_phone TEXT;

-- Rename address_line to full_address if it exists and full_address doesn't
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'addresses' AND column_name = 'address_line')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'addresses' AND column_name = 'full_address') THEN
        ALTER TABLE public.addresses RENAME COLUMN address_line TO full_address;
    END IF;
END $$;

-- Rename city to label if label was just added and is empty, or just ensure both exist
-- Actually, the model uses 'full_address' and 'label'.
-- Let's just make the DB match the model exactly as defined in schema.sql
