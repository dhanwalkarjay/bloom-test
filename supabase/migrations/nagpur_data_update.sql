-- 1. Nagpur Florals Shop (Updated coordinates)
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

-- 2. Products for Nagpur Florals (Ensuring New Arrival and Seasonal flags)
INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Blushing Peonies',
    'Elegant peonies with soft pink petals.',
    52.00,
    'https://images.pexels.com/photos/931162/pexels-photo-931162.jpeg?auto=compress&cs=tinysrgb&w=300',
    'New Arrival',
    false,
    ARRAY['Birthday', 'Romance'],
    false,
    false,
    true
FROM shops WHERE name = 'Nagpur Florals'
ON CONFLICT DO NOTHING;

INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Lavender Fields',
    'A calming bunch of fresh lavender.',
    38.00,
    'https://images.pexels.com/photos/1367192/pexels-photo-1367192.jpeg?auto=compress&cs=tinysrgb&w=300',
    'New Arrival',
    false,
    ARRAY['Get Well', 'Relax'],
    false,
    false,
    true
FROM shops WHERE name = 'Nagpur Florals'
ON CONFLICT DO NOTHING;

INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal, is_new_arrival)
SELECT
    id,
    'Sunlit Serenade',
    'Bright sunflowers arranged to perfection.',
    48.00,
    'https://images.pexels.com/photos/1408221/pexels-photo-1408221.jpeg?auto=compress&cs=tinysrgb&w=300',
    'Bestseller',
    false,
    ARRAY['Birthday', 'Congratulation'],
    true,
    false,
    false
FROM shops WHERE name = 'Nagpur Florals'
ON CONFLICT DO NOTHING;
