-- Migration Script to fix schema inconsistencies

-- 1. Fix Shops table
ALTER TABLE shops ADD COLUMN IF NOT EXISTS tier TEXT DEFAULT 'Standard';
ALTER TABLE shops ADD COLUMN IF NOT EXISTS preparation_minutes INTEGER DEFAULT 30;

-- 2. Fix Products table
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='name') THEN
        ALTER TABLE products RENAME COLUMN name TO title;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='image_url') THEN
        ALTER TABLE products RENAME COLUMN image_url TO images;
    END IF;
END $$;

ALTER TABLE products ADD COLUMN IF NOT EXISTS occasion_tags TEXT[];
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_bestseller BOOLEAN DEFAULT false;
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_seasonal BOOLEAN DEFAULT false;
ALTER TABLE products ADD COLUMN IF NOT EXISTS is_new_arrival BOOLEAN DEFAULT false;
ALTER TABLE products ADD COLUMN IF NOT EXISTS stock_count INTEGER DEFAULT 100;

-- 3. Drop functions to avoid overloading issues
DROP FUNCTION IF EXISTS nearby_shops(double precision, double precision);
DROP FUNCTION IF EXISTS nearby_shops(double precision, double precision, double precision);
DROP FUNCTION IF EXISTS search_products_nearby(double precision, double precision, text, text, double precision);
