-- Advanced Seed Data for Bloom

-- 1. Ensure Nagpur Florals exists
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
) ON CONFLICT DO NOTHING;

-- 2. Clear existing sample products to avoid duplicates if necessary, or just use unique names
DELETE FROM products WHERE title IN ('Blushing Peonies', 'Lavender Fields', 'Sunlit Serenade', 'Autumn Harvest', 'Winter White', 'Midnight Rose', 'Golden Majesty');

-- 3. New Arrivals (is_new_arrival = true)
INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Blushing Peonies',
    'Soft pink peonies for a delicate touch.',
    52.00,
    'https://images.pexels.com/photos/931162/pexels-photo-931162.jpeg?auto=compress&cs=tinysrgb&w=400',
    'Romance',
    false,
    ARRAY['Birthday', 'Anniversary'],
    false,
    false,
    true
FROM shops WHERE name = 'Nagpur Florals' LIMIT 1;

INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Lavender Fields',
    'A calming bunch of fresh French lavender.',
    38.00,
    'https://images.pexels.com/photos/1367192/pexels-photo-1367192.jpeg?auto=compress&cs=tinysrgb&w=400',
    'Relaxation',
    false,
    ARRAY['Get Well', 'Sympathy'],
    false,
    false,
    true
FROM shops WHERE name = 'Nagpur Florals' LIMIT 1;

-- 4. Seasonal Favorites (is_seasonal = true)
INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Autumn Harvest',
    'Warm tones of orange and gold.',
    45.00,
    'https://images.pexels.com/photos/1519336/pexels-photo-1519336.jpeg?auto=compress&cs=tinysrgb&w=400',
    'Seasonal',
    false,
    ARRAY['Thanksgiving', 'Fall'],
    false,
    true,
    false
FROM shops WHERE name = 'Nagpur Florals' LIMIT 1;

INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Winter White',
    'Pristine white lilies and roses.',
    60.00,
    'https://images.pexels.com/photos/1367192/pexels-photo-1367192.jpeg?auto=compress&cs=tinysrgb&w=400',
    'Seasonal',
    false,
    ARRAY['Winter', 'Elegance'],
    false,
    true,
    false
FROM shops WHERE name = 'Nagpur Florals' LIMIT 1;

-- 5. Bestsellers (is_bestseller = true)
INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Sunlit Serenade',
    'The most popular sunflower arrangement.',
    48.00,
    'https://images.pexels.com/photos/1408221/pexels-photo-1408221.jpeg?auto=compress&cs=tinysrgb&w=400',
    'Birthday',
    false,
    ARRAY['Birthday', 'Cheer'],
    true,
    false,
    false
FROM shops WHERE name = 'Nagpur Florals' LIMIT 1;

INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Midnight Rose',
    'Mysterious and dark, a crowd favorite.',
    29.99,
    'https://images.pexels.com/photos/1563356/pexels-photo-1563356.jpeg?auto=compress&cs=tinysrgb&w=400',
    'Romance',
    false,
    ARRAY['Anniversary', 'Night'],
    true,
    false,
    false
FROM shops WHERE name = 'Nagpur Florals' LIMIT 1;

-- 6. Luxury (is_lux = true)
INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Golden Majesty',
    'Gold-trimmed lilies and rare blooms.',
    249.99,
    'https://images.pexels.com/photos/1036622/pexels-photo-1036622.jpeg?auto=compress&cs=tinysrgb&w=400',
    'Luxury',
    true,
    ARRAY['VIP', 'Exclusive'],
    true,
    false,
    false
FROM shops WHERE name = 'Nagpur Florals' LIMIT 1;
