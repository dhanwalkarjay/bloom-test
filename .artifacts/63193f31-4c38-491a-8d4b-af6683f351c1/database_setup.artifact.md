# Bloom Database Setup - Supabase SQL

Run the following SQL in your **Supabase SQL Editor** to create all necessary tables, enable PostGIS for location discovery, and set up Row Level Security (RLS).

## 1. Enable Extensions
```sql
-- Enable PostGIS for location-based queries
create extension if not exists postgis;
```

## 2. Create Tables

### Shops & Products
```sql
-- Shops (Florists)
create table shops (
  id uuid default gen_random_uuid() primary key,
  name text not null,
  rating numeric(2,1) default 5.0,
  is_open boolean default true,
  prep_time text, -- e.g., '30-40 mins'
  image_url text,
  opens_at text, -- e.g., '09:00 AM'
  location geography(point, 4326) -- PostGIS point for GPS
);

-- Products (Bouquets)
create table products (
  id uuid default gen_random_uuid() primary key,
  shop_id uuid references shops(id) on delete cascade,
  name text not null,
  description text,
  price numeric(10,2) not null,
  image_url text,
  category text, -- e.g., 'Anniversary', 'Birthday'
  is_lux boolean default false,
  created_at timestamp with time zone default now()
);

-- Addons (Wraps, ribbons, cards)
create table addons (
  id uuid default gen_random_uuid() primary key,
  name text not null,
  price numeric(10,2) not null,
  image_url text,
  type text -- 'wrap', 'ribbon', 'message_card'
);
```

### User Data & Orders
```sql
-- User Profiles (linked to auth.users)
create table profiles (
  id uuid references auth.users on delete cascade primary key,
  full_name text,
  phone text unique,
  avatar_url text,
  updated_at timestamp with time zone default now()
);

-- Delivery Addresses
create table addresses (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references profiles(id) on delete cascade,
  address_line text not null,
  city text not null,
  latitude float8,
  longitude float8,
  is_default boolean default false
);

-- Orders
create table orders (
  id uuid default gen_random_uuid() primary key,
  user_id uuid references profiles(id),
  shop_id uuid references shops(id),
  address_id uuid references addresses(id),
  total_amount numeric(10,2) not null,
  status text default 'Placed', -- 'Placed', 'Confirmed', 'Preparing', 'Out for Delivery', 'Delivered'
  payment_status text default 'Pending',
  delivery_slot text,
  created_at timestamp with time zone default now()
);

-- Order Items
create table order_items (
  id uuid default gen_random_uuid() primary key,
  order_id uuid references orders(id) on delete cascade,
  product_id uuid references products(id),
  quantity integer not null default 1,
  unit_price numeric(10,2) not null,
  size text default 'Regular',
  card_message text
);

-- Reviews
create table reviews (
  id uuid default gen_random_uuid() primary key,
  order_id uuid references orders(id) on delete cascade,
  user_id uuid references profiles(id),
  rating integer check (rating >= 1 and rating <= 5),
  comment text,
  created_at timestamp with time zone default now()
);
```

## 3. Row Level Security (RLS)
```sql
-- Enable RLS on all tables
alter table profiles enable row level security;
alter table shops enable row level security;
alter table products enable row level security;
alter table addons enable row level security;
alter table addresses enable row level security;
alter table orders enable row level security;
alter table order_items enable row level security;
alter table reviews enable row level security;

-- Public access (Read-only for discovery)
create policy "Shops are viewable by everyone" on shops for select using (true);
create policy "Products are viewable by everyone" on products for select using (true);
create policy "Addons are viewable by everyone" on addons for select using (true);

-- User-specific access
create policy "Users can view their own profiles" on profiles for select using (auth.uid() = id);
create policy "Users can update their own profiles" on profiles for update using (auth.uid() = id);

create policy "Users can manage their own addresses" on addresses for all using (auth.uid() = user_id);

create policy "Users can view their own orders" on orders for select using (auth.uid() = user_id);
create policy "Users can insert their own orders" on orders for insert with check (auth.uid() = user_id);

create policy "Users can view their own order items" on order_items for select using (
  exists (select 1 from orders where orders.id = order_id and orders.user_id = auth.uid())
);
create policy "Users can insert their own order items" on order_items for insert with check (
  exists (select 1 from orders where orders.id = order_id and orders.user_id = auth.uid())
);

create policy "Users can manage their own reviews" on reviews for all using (auth.uid() = user_id);
```

## 4. Location Search Function (RPC)
```sql
-- RPC for nearby shops
create or replace function nearby_shops(lat float8, lng float8)
returns table (
  id uuid,
  name text,
  rating numeric,
  is_open boolean,
  prep_time text,
  image_url text,
  opens_at text,
  distance float8
)
language sql
stable
as $$
  select
    id, name, rating, is_open, prep_time, image_url, opens_at,
    st_distance(location, st_setsrid(st_point(lng, lat), 4326)::geography) as distance
  from shops
  order by is_open desc, distance asc
  limit 20;
$$;
```

> [!TIP]
> After running this, add some dummy data to the `shops` table with `st_point(longitude, latitude)` to see results on your Home screen!
