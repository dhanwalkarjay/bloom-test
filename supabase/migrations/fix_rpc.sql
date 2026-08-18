-- Fix RPC functions for Bloom app

-- 1. nearby_shops
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

-- 2. search_products_nearby
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
