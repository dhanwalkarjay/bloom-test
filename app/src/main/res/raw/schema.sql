-- Bloom Floral Database Schema (Supabase/PostgreSQL)

-- 1. Enable PostGIS for location features
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Shops Table
CREATE TABLE shops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    rating DECIMAL(2,1) DEFAULT 4.0,
    is_open BOOLEAN DEFAULT true,
    preparation_minutes INTEGER DEFAULT 30,
    prep_time TEXT DEFAULT '30-40 mins',
    image_url TEXT,
    opens_at TEXT DEFAULT '8:00 AM',
    tier TEXT DEFAULT 'Standard',
    location GEOGRAPHY(POINT) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. Products Table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    shop_id UUID REFERENCES shops(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    images TEXT, -- URL to main image
    category TEXT,
    is_lux BOOLEAN DEFAULT false,
    occasion_tags TEXT[],
    is_bestseller BOOLEAN DEFAULT false,
    is_seasonal BOOLEAN DEFAULT false,
    is_new_arrival BOOLEAN DEFAULT false,
    stock_count INTEGER DEFAULT 100,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. Profiles Table (User Data)
CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users ON DELETE CASCADE,
    full_name TEXT,
    phone TEXT UNIQUE,
    avatar_url TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. Addresses Table
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    label TEXT, -- e.g., 'Home', 'Office'
    full_address TEXT NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    recipient_name TEXT,
    recipient_phone TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. Notifications Table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT DEFAULT 'info', -- 'info', 'order', 'promo'
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. Profile Sync Trigger
-- Automatically create a profile when a new user signs up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, full_name, phone)
    VALUES (new.id, new.raw_user_meta_data->>'full_name', new.phone);
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 8. RPC Function for Nearby Shops
CREATE OR REPLACE FUNCTION nearby_shops(lat DOUBLE PRECISION, lng DOUBLE PRECISION, radius_km DOUBLE PRECISION DEFAULT 10.0)
RETURNS TABLE (
    id UUID,
    name TEXT,
    rating DECIMAL,
    is_open BOOLEAN,
    prep_time TEXT,
    image_url TEXT,
    opens_at TEXT,
    tier TEXT,
    distance DOUBLE PRECISION
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        s.id,
        s.name,
        s.rating,
        s.is_open,
        s.prep_time,
        s.image_url,
        s.opens_at,
        s.tier,
        ST_Distance(s.location, ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography) AS distance
    FROM
        shops s
    WHERE
        ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography, radius_km * 1000)
    ORDER BY
        distance ASC;
END;
$$ LANGUAGE plpgsql;

-- 7. RPC Function for Nearby Product Search
CREATE OR REPLACE FUNCTION search_products_nearby(
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    search_query TEXT DEFAULT NULL,
    cat_filter TEXT DEFAULT NULL,
    radius_km DOUBLE PRECISION DEFAULT 10.0
)
RETURNS TABLE (
    product_id UUID,
    shop_id UUID,
    title TEXT,
    description TEXT,
    price DECIMAL,
    images TEXT,
    category TEXT,
    is_lux BOOLEAN,
    distance DOUBLE PRECISION,
    shop_name TEXT,
    is_shop_open BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id as product_id,
        s.id as shop_id,
        p.title,
        p.description,
        p.price,
        p.images,
        p.category,
        p.is_lux,
        ST_Distance(s.location, ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography) AS distance,
        s.name as shop_name,
        s.is_open as is_shop_open
    FROM
        products p
    JOIN
        shops s ON p.shop_id = s.id
    WHERE
        ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography, radius_km * 1000)
        AND (search_query IS NULL OR p.title ILIKE '%' || search_query || '%')
        AND (cat_filter IS NULL OR p.category = cat_filter)
    ORDER BY
        distance ASC;
END;
$$ LANGUAGE plpgsql;

-- 10. Seed Data

-- Insert a Shop in NYC
INSERT INTO shops (name, rating, is_open, prep_time, image_url, opens_at, tier, location)
VALUES (
    'Blossom Haven',
    4.8,
    true,
    '25-35 mins',
    'https://images.unsplash.com/photo-1519336367661-eba9c1dfa5e9',
    '9:00 AM',
    'Standard',
    ST_SetSRID(ST_MakePoint(-74.006, 40.7128), 4326)::geography
);

-- Insert a Shop in Nagpur
INSERT INTO shops (name, rating, is_open, prep_time, image_url, opens_at, tier, location)
VALUES (
    'Nagpur Florals',
    4.7,
    true,
    '20-30 mins',
    'https://images.pexels.com/photos/1166644/pexels-photo-1166644.jpeg?auto=compress&cs=tinysrgb&w=600',
    '8:00 AM',
    'Standard',
    ST_SetSRID(ST_MakePoint(79.0882, 21.1458), 4326)::geography
);

-- Insert Products for NYC shop
INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal)
SELECT
    id,
    'Midnight Rose Bouquet',
    'A stunning arrangement of deep red roses and lilies.',
    49.99,
    'https://images.unsplash.com/photo-1522673607200-164883eeca48',
    'Anniversary',
    true,
    ARRAY['Anniversary', 'Love'],
    true,
    true
FROM shops WHERE name = 'Blossom Haven';

-- Insert Products for Nagpur shop
INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal)
SELECT
    id,
    'Nagpur Orange Lily',
    'Fresh lilies with a vibrant citrus hue, local to Nagpur.',
    19.99,
    'https://images.pexels.com/photos/1408221/pexels-photo-1408221.jpeg?auto=compress&cs=tinysrgb&w=300',
    'Birthday',
    false,
    ARRAY['Birthday', 'Local'],
    true,
    true
FROM shops WHERE name = 'Nagpur Florals';

INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal)
SELECT
    id,
    'Regal Gold Collection',
    'Luxury arrangement for high-end celebrations.',
    99.99,
    'https://images.pexels.com/photos/1036622/pexels-photo-1036622.jpeg?auto=compress&cs=tinysrgb&w=300',
    'Wedding',
    true,
    ARRAY['Wedding', 'Luxury'],
    false,
    false
FROM shops WHERE name = 'Nagpur Florals';

