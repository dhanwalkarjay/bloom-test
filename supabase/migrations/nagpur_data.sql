-- 1. Nagpur Florals Shop
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

-- 2. Products for Nagpur Florals
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

INSERT INTO products (shop_id, title, description, price, images, category, is_lux, occasion_tags, is_bestseller, is_seasonal)
SELECT
    id,
    'Nagpur Midnight Rose',
    'Elegant dark roses locally sourced.',
    29.99,
    'https://images.pexels.com/photos/1563356/pexels-photo-1563356.jpeg?auto=compress&cs=tinysrgb&w=300',
    'Anniversary',
    false,
    ARRAY['Anniversary', 'Classic'],
    false,
    true
FROM shops WHERE name = 'Nagpur Florals';
