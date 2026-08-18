-- Add mock data for existing shops to test different ranges/times
UPDATE public.shops SET delivery_radius_km = 3.0, closes_at = '20:00' WHERE name ILIKE '%Local%';
UPDATE public.shops SET delivery_radius_km = 8.0, closes_at = '23:30' WHERE tier = 'lux';
UPDATE public.shops SET delivery_radius_km = 5.0, closes_at = '21:00' WHERE delivery_radius_km IS NULL;
