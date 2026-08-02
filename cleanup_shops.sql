-- Delete duplicate Nagpur Florals, keeping only one
DELETE FROM shops
WHERE name = 'Nagpur Florals'
AND id NOT IN (SELECT MIN(id) FROM shops WHERE name = 'Nagpur Florals');

-- Update some shop names to be more distinct
UPDATE shops SET name = 'Rosie''s Petals' WHERE name = 'Blossom Haven' AND id = (SELECT MIN(id) FROM shops WHERE name = 'Blossom Haven');
UPDATE shops SET name = 'The Bloom Bar' WHERE name = 'Blossom Haven';
UPDATE shops SET name = 'Nagpur Central Florals' WHERE name = 'Nagpur Florals';

-- Ensure all shops have a location in Nagpur for testing
UPDATE shops SET location = ST_SetSRID(ST_MakePoint(79.0882, 21.1458), 4326)::geography;
