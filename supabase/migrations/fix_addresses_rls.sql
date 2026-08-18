-- Ensure all existing users have a profile
INSERT INTO public.profiles (id, full_name, phone)
SELECT id, raw_user_meta_data->>'full_name', phone
FROM auth.users
ON CONFLICT (id) DO NOTHING;

-- Drop existing policies if any
DROP POLICY IF EXISTS "Users can manage their own addresses" ON public.addresses;

-- Create more robust policies
CREATE POLICY "Enable insert for authenticated users only"
ON public.addresses FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Enable select for users based on user_id"
ON public.addresses FOR SELECT
TO authenticated
USING (auth.uid() = user_id);

CREATE POLICY "Enable update for users based on user_id"
ON public.addresses FOR UPDATE
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Enable delete for users based on user_id"
ON public.addresses FOR DELETE
TO authenticated
USING (auth.uid() = user_id);

-- Ensure RLS is enabled
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;
