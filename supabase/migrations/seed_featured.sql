-- Seed featured products for NagpurCentral Florals

DO $$
DECLARE
    v_shop_id UUID;
BEGIN
    SELECT id INTO v_shop_id FROM shops WHERE name = 'Nagpur Central Florals' LIMIT 1;

    -- New Arrivals
    INSERT INTO products (shop_id, title, description, price, images, category, is_lux, is_new_arrival, is_seasonal, is_bestseller)
    VALUES
    (v_shop_id, 'Spring Breeze', 'Fresh spring lilies', 45.00, 'https://images.pexels.com/photos/1036622/pexels-photo-1036622.jpeg?auto=compress&cs=tinysrgb&w=400', 'Spring', false, true, false, false),
    (v_shop_id, 'Midnight Orchid', 'Rare dark orchids', 85.00, 'https://images.pexels.com/photos/1563356/pexels-photo-1563356.jpeg?auto=compress&cs=tinysrgb&w=400', 'Luxury', true, true, false, false);

    -- Seasonal
    INSERT INTO products (shop_id, title, description, price, images, category, is_lux, is_new_arrival, is_seasonal, is_bestseller)
    VALUES
    (v_shop_id, 'Summer Sun', 'Bright yellow sunflowers', 35.00, 'https://images.pexels.com/photos/1408221/pexels-photo-1408221.jpeg?auto=compress&cs=tinysrgb&w=400', 'Seasonal', false, false, true, false),
    (v_shop_id, 'Winter Frost', 'White roses and silver leaves', 55.00, 'https://images.pexels.com/photos/1166644/pexels-photo-1166644.jpeg?auto=compress&cs=tinysrgb&w=400', 'Seasonal', false, false, true, false);

    -- Bestsellers
    INSERT INTO products (shop_id, title, description, price, images, category, is_lux, is_new_arrival, is_seasonal, is_bestseller)
    VALUES
    (v_shop_id, 'Classic Red Roses', 'Best-selling dozen red roses', 60.00, 'https://images.pexels.com/photos/1519336/pexels-photo-1519336.jpeg?auto=compress&cs=tinysrgb&w=400', 'Romance', false, false, false, true),
    (v_shop_id, 'Mixed Tulip Box', 'Colorful tulip arrangement', 40.00, 'https://images.pexels.com/photos/931162/pexels-photo-931162.jpeg?auto=compress&cs=tinysrgb&w=400', 'Birthday', false, false, false, true);

END $$;
