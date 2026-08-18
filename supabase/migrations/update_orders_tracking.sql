-- Add tracking specific columns to orders table
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS delivery_otp TEXT;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS rider_name TEXT;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS rider_phone TEXT;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS rider_lat DOUBLE PRECISION;
ALTER TABLE public.orders ADD COLUMN IF NOT EXISTS rider_lng DOUBLE PRECISION;

-- Update RLS for these new columns if necessary (usually public.orders is already open for authenticated users)
