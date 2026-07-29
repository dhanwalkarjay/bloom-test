# Bloom — Complete SRS & System Design (Rebuild v3)
### Native Android (Java) + Fresh Supabase Backend — Single Source of Truth

This document assumes nothing exists yet. It consolidates every decision made across this project's history into one clean, buildable specification.

---

## 1. Product Vision

Bloom is a location-first flower marketplace. Customers discover open flower shops near them by GPS, browse and customize bouquets, and get them delivered on their chosen timeline. Florists run their shop and their own delivery team through a dedicated partner app. Admins moderate the platform and control rollout of new features through a web dashboard.

---

## 2. System Architecture

**Three separate applications, one shared backend:**

```
┌─────────────────────┐   ┌──────────────────────────┐   ┌──────────────────┐
│   Customer App        │   │   Bloom Partner App        │   │   Admin Console    │
│   Native Android      │   │   Native Android            │   │   Web (browser)    │
│   (Java)               │   │   (Java) — ONE binary,      │   │                    │
│                        │   │   two views by role:        │   │                    │
│                        │   │   Florist Owner / Agent     │   │                    │
└──────────┬─────────────┘   └──────────┬───────────────────┘   └─────────┬──────────┘
           │                            │                                  │
           └────────────────────────────┴──────────────────────────────────┘
                                         │
                              ┌──────────▼──────────────┐
                              │        Supabase           │
                              │  Postgres + PostGIS        │
                              │  Auth (Phone OTP)           │
                              │  Storage                    │
                              │  Edge Functions              │
                              └──────────┬──────────────┘
                                         │
                         ┌───────────────┼────────────────┐
                         ▼               ▼                ▼
                     Razorpay        Firebase          (future: RazorpayX)
                    (payments)   (push notifications)
```

**Why one binary for Florist + Agent:** delivery agents belong to a specific florist, not the platform independently — they're added by their florist, log in with their own phone/OTP on their own device, and the app shows them a reduced "Agent view" automatically once recognized. This avoids agents ever needing the florist's phone or credentials.

**Communication pattern (native Android):** the app talks to Supabase entirely over HTTPS — Auth endpoints for login, PostgREST endpoints for table reads/writes, RPC endpoints for calling Postgres functions, and Edge Function endpoints for payments/notifications. Every request carries the `anon` key plus, once logged in, the user's JWT access token, which is what RLS checks on every query.

---

## 3. Actors & Roles

| Role | App | Can do |
|---|---|---|
| **Customer** | Customer App | Discover nearby shops, browse, customize, order, pay, track, review |
| **Florist Owner** | Bloom Partner App (Owner view) | Manage shop, list products/add-ons/coupons, handle orders, manage delivery agents, view earnings, request payouts |
| **Delivery Agent** | Bloom Partner App (Agent view) | See assigned deliveries, navigate, mark delivered — nothing else |
| **Admin** | Admin Console (web) | Approve florists/products, manage disputes, manage feature flags, view analytics, process payouts |

Role storage uses a `user_roles` table plus a `has_role()` function (rather than a single column on `profiles`) — this is the pattern that proved out well in earlier development, and avoids awkward RLS recursion issues a simple column can cause.

Delivery Agents are a special case: they're not a platform-wide role assigned at signup — they're **linked to a specific florist** via an invite-and-match system (Section 7).

---

## 4. Functional Requirements

### 4.1 Authentication
- **FR-1:** Phone number + OTP + password hybrid — OTP required only at first signup to verify phone ownership; password login for all returning sessions.
- **FR-2:** New users select a role (Customer or Florist) before OTP verification.
- **FR-3:** On login, the system checks whether the phone number matches a pending Delivery Agent invite; if so, the user is routed to the Agent view regardless of any other role.
- **FR-4:** Returning users route automatically to their correct app/view based on stored role — never re-prompted.

### 4.2 Location & Discovery (Customer)
- **FR-5:** Request GPS location on Home load; fall back to manual location entry if denied.
- **FR-6:** Show shops within a configurable radius, sorted open-first, then nearest, then highest-rated.
- **FR-7:** Closed shops remain visible but visually deprioritized, not hidden.
- **FR-8:** Tapping a shop opens its dedicated product catalog.

### 4.3 Browsing, Customization, Cart (Customer)
- **FR-9:** Customization limited to pre-listed bouquet variants (size, wrap, ribbon, message card) — full custom-from-scratch bouquets remain a flagged "Coming Soon" feature.
- **FR-10:** Add-ons and coupons are scoped per shop (each florist manages their own).
- **FR-11:** Cart is single-vendor — one shop per order.

### 4.4 Delivery Timing (Customer)
- **FR-12:** Instant, Same-Day, Scheduled are functional; Midnight is feature-flagged "Coming Soon."
- **FR-13:** Delivery fee is calculated by real distance (florist location to delivery address) at checkout, snapshotted onto the order.

### 4.5 Checkout & Payment
- **FR-14:** Razorpay integration is server-verified — order creation and payment signature verification both happen in Supabase Edge Functions; the Razorpay secret key never reaches the client.
- **FR-15:** Cash on Delivery is supported as an alternative payment path.
- **FR-16:** Checkout displays an itemized breakdown: bouquet subtotal, add-ons, delivery fee, platform fee, tax, total — never a single lump sum.

### 4.6 Order Lifecycle
- **FR-17:** Status progresses strictly: `placed → confirmed → preparing → out_for_delivery → delivered` (or `placed → cancelled`).
- **FR-18:** When confirming an order, the Florist Owner selects a delivery method: assign to one of their own active Delivery Agents, or "I'll deliver it myself." Third-party delivery methods remain feature-flagged.
- **FR-19:** Customer's order tracking updates live via Supabase Realtime.
- **FR-20:** Reviews can only be left on orders marked `delivered`.

### 4.7 Delivery Agent Management (Florist Owner) — NEW
- **FR-21:** A Florist Owner can add an agent by entering name and phone number, creating a `pending` invite.
- **FR-22:** When that phone number logs into the Bloom Partner app, it auto-links to the pending invite and activates the agent.
- **FR-23:** A Florist Owner can deactivate an agent at any time, immediately revoking their ability to see or act on new orders (existing assigned orders remain visible until completed).
- **FR-24:** A Florist Owner sees each agent's real-time on-duty/off-duty status.

### 4.8 Delivery Agent Operations (Agent) — NEW
- **FR-25:** An agent toggles their own On Duty / Off Duty status.
- **FR-26:** An agent sees only orders assigned to them, never another agent's or another florist's orders.
- **FR-27:** An agent can mark an assigned order "Delivered" — this is the only order-state-changing action available to them, enforced server-side, not just hidden in the UI.
- **FR-28:** An agent can open native maps navigation to the delivery address.

### 4.9 Florist Shop & Inventory Management
- **FR-29:** New florist shops and products default to `pending`, requiring Admin approval before customer visibility.
- **FR-30:** Florist Owner manages products, add-ons, and shop-specific coupons independently.
- **FR-31:** Florist Owner has a dashboard with today's stats and an actionable incoming-orders queue.

### 4.10 Payout System
- **FR-32:** Every delivered order generates a ledger entry — a **credit** to the florist for prepaid orders (platform owes them), or a **debit** for COD orders (they owe the platform commission, since they already collected the cash directly).
- **FR-33:** Commission applies only to bouquet + add-on value — never to delivery fee, platform fee, or tax.
- **FR-34:** Delivery fee is paid in full to the florist for self/agent-delivered orders (no commission taken).
- **FR-35:** Earnings enter a 48-hour hold before becoming payout-eligible, to allow for disputes/refunds.
- **FR-36:** A scheduled batch job nets each florist's credits and debits weekly, generating a payout batch when the net result exceeds a minimum threshold.
- **FR-37:** Admin reviews, approves or rejects, and marks payout batches as paid (with a required transaction reference), which is the point money actually leaves the business's bank account.
- **FR-38:** Refunds on already-earned orders create a clawback entry, reducing the next payout rather than attempting to reverse money already sent.
- **FR-39:** Florists with a persistent negative balance (COD-heavy, no prepaid orders to net against) are flagged for manual admin collection follow-up.

### 4.11 Admin
- **FR-40:** Approve/reject pending florists and products.
- **FR-41:** View platform KPIs: orders, revenue, active shops, delivery method/timing breakdowns.
- **FR-42:** Manage feature flags (data-driven, no redeploy needed to toggle).
- **FR-43:** Resolve disputes raised by customers or florists.
- **FR-44:** Review and act on payout batches (Section 4.10).

---

## 5. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-1 | Nearby-shops query returns in under 1 second at a 10km radius, via spatial index. |
| NFR-2 | All financial and role-changing operations are enforced server-side via RLS and `SECURITY DEFINER` functions with explicit role checks — never trusted from client input alone. |
| NFR-3 | Every `SECURITY DEFINER` function callable via the API must either check the caller's role internally, or be revoked from `PUBLIC`/`authenticated`/`anon` if it's meant to be trigger-only. |
| NFR-4 | An agent's app can only ever change the status of orders explicitly assigned to them — enforced in the database function itself, not just UI-hidden. |
| NFR-5 | The ledger is append-only; entries are never updated or deleted, only added, including corrections (which are new offsetting entries, not edits). |
| NFR-6 | Core browse-to-order flow degrades gracefully without GPS. |
| NFR-7 | "Coming Soon" features are visually unambiguous and non-interactive. |
| NFR-8 | Native Android UI — no WebView-based rendering, genuine native scroll/animation/gesture behavior throughout. |
| NFR-9 | Secrets (Razorpay key secret, Firebase service account, Supabase service role key) never appear in any client-side code, only in Supabase Edge Function secrets. |

---

## 6. Database Design (fresh build)

```sql
create extension if not exists postgis;
create extension if not exists pgcrypto;

-- ROLES
create type public.app_role as enum ('customer', 'florist', 'admin');

create table public.profiles (
  id uuid references auth.users(id) primary key,
  name text, phone text unique, email text, country text,
  preferred_currency text default 'INR',
  preferred_language text default 'en',
  created_at timestamptz default now()
);

create table public.user_roles (
  user_id uuid references public.profiles(id) not null,
  role public.app_role not null,
  primary key (user_id, role)
);

create or replace function public.has_role(_user_id uuid, _role public.app_role)
returns boolean
language sql stable security definer set search_path = 'public'
as $$
  select exists (select 1 from public.user_roles where user_id = _user_id and role = _role);
$$;

-- ADDRESSES
create table public.addresses (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.profiles(id) not null,
  label text, full_address text not null,
  latitude double precision, longitude double precision,
  recipient_name text, recipient_phone text,
  created_at timestamptz default now()
);

-- FLORISTS
create table public.florists (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.profiles(id) not null unique,
  shop_name text not null, country text, city text,
  latitude double precision, longitude double precision,
  location geography(Point, 4326),
  tier text default 'regular' check (tier in ('regular','lux')),
  rating numeric default 0, is_open boolean default true,
  preparation_minutes int default 30,
  status text default 'pending' check (status in ('pending','approved','rejected')),
  approved_by uuid references public.profiles(id), approved_at timestamptz,
  created_at timestamptz default now()
);

create or replace function public.sync_florist_location() returns trigger as $$
begin
  if new.latitude is not null and new.longitude is not null then
    new.location := ST_SetSRID(ST_MakePoint(new.longitude, new.latitude), 4326)::geography;
  end if;
  return new;
end; $$ language plpgsql;

create trigger trg_sync_florist_location before insert or update on public.florists
for each row execute function public.sync_florist_location();

create index idx_florists_location on public.florists using gist(location);

-- DELIVERY AGENTS (new)
create table public.delivery_agents (
  id uuid primary key default gen_random_uuid(),
  florist_id uuid references public.florists(id) not null,
  user_id uuid references public.profiles(id),
  name text not null, phone text not null,
  status text default 'pending' check (status in ('pending','active','inactive')),
  is_available boolean default false,
  created_at timestamptz default now()
);

create or replace function public.link_delivery_agent(p_phone text)
returns uuid
language plpgsql security definer set search_path = 'public'
as $$
declare v_agent_id uuid;
begin
  update public.delivery_agents
  set user_id = auth.uid(), status = 'active'
  where phone = p_phone and status = 'pending' and user_id is null
  returning id into v_agent_id;
  return v_agent_id;
end; $$;

-- PRODUCTS / ADDONS / COUPONS
create table public.products (
  id uuid primary key default gen_random_uuid(),
  florist_id uuid references public.florists(id) not null,
  title text not null, description text, category text,
  occasion_tags text[], color_tags text[],
  price numeric not null, currency text default 'INR',
  is_lux boolean default false, designer_note text,
  images text[], stock_count int default 0, is_bestseller boolean default false,
  status text default 'pending' check (status in ('pending','approved','rejected')),
  approved_by uuid references public.profiles(id), approved_at timestamptz,
  created_at timestamptz default now()
);

create table public.addons (
  id uuid primary key default gen_random_uuid(),
  florist_id uuid references public.florists(id) not null,
  name text not null, price numeric not null, currency text default 'INR',
  image text, is_available boolean default true
);

create table public.coupons (
  id uuid primary key default gen_random_uuid(),
  florist_id uuid references public.florists(id) not null,
  code text not null, discount_type text check (discount_type in ('percentage','flat')),
  value numeric not null, start_date date, end_date date,
  is_active boolean default true, usage_count int default 0,
  unique (florist_id, code)
);

-- ORDERS (itemized pricing)
create table public.orders (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.profiles(id) not null,
  florist_id uuid references public.florists(id) not null,
  address_id uuid references public.addresses(id) not null,
  delivery_agent_id uuid references public.delivery_agents(id),
  delivery_slot_type text default 'same_day' check (delivery_slot_type in ('instant','same_day','scheduled','midnight')),
  scheduled_for timestamptz,
  delivery_method text default 'own_delivery_boy' check (delivery_method in ('own_delivery_boy','bloom_partner','third_party_courier')),
  status text default 'placed' check (status in ('placed','confirmed','preparing','out_for_delivery','delivered','cancelled')),
  bouquet_subtotal numeric, addons_subtotal numeric, delivery_fee numeric,
  platform_fee numeric default 0, tax_amount numeric default 0, discount_amount numeric default 0,
  delivery_distance_km numeric,
  commission_rate numeric default 0.15, commission_amount numeric, florist_earning numeric,
  total_amount numeric generated always as (
    coalesce(bouquet_subtotal,0) + coalesce(addons_subtotal,0) + coalesce(delivery_fee,0)
    + coalesce(platform_fee,0) + coalesce(tax_amount,0) - coalesce(discount_amount,0)
  ) stored,
  currency text default 'INR',
  razorpay_order_id text, razorpay_payment_id text,
  payment_status text default 'pending' check (payment_status in ('pending','paid','failed','refunded','cod')),
  payout_eligible_at timestamptz,
  created_at timestamptz default now()
);

create table public.order_items (
  id uuid primary key default gen_random_uuid(),
  order_id uuid references public.orders(id) not null,
  product_id uuid references public.products(id) not null,
  product_name text not null, quantity int default 1,
  customization text, unit_price numeric not null
);

create table public.order_addons (
  id uuid primary key default gen_random_uuid(),
  order_id uuid references public.orders(id) not null,
  addon_id uuid references public.addons(id) not null,
  addon_name text not null, unit_price numeric not null
);

-- REVIEWS / DISPUTES / SUPPORT
create table public.reviews (
  id uuid primary key default gen_random_uuid(),
  order_id uuid references public.orders(id) not null,
  user_id uuid references public.profiles(id) not null,
  rating int check (rating between 1 and 5), comment text, photos text[],
  created_at timestamptz default now()
);

create table public.disputes (
  id uuid primary key default gen_random_uuid(),
  order_id uuid references public.orders(id) not null,
  raised_by uuid references public.profiles(id) not null,
  reason text not null, status text default 'open' check (status in ('open','resolved')),
  resolution_note text, created_at timestamptz default now(), resolved_at timestamptz
);

create table public.support_tickets (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.profiles(id),
  florist_id uuid references public.florists(id),
  subject text not null, status text default 'open' check (status in ('open','resolved')),
  created_at timestamptz default now(), updated_at timestamptz default now()
);

create table public.support_messages (
  id uuid primary key default gen_random_uuid(),
  ticket_id uuid references public.support_tickets(id) not null,
  sender_id uuid references public.profiles(id) not null,
  message text not null, created_at timestamptz default now()
);

-- FEATURE FLAGS
create table public.feature_flags (
  key text primary key, enabled boolean default false, label text, updated_at timestamptz default now()
);
insert into public.feature_flags (key, enabled, label) values
  ('midnight_delivery', false, 'Midnight Delivery'),
  ('custom_bouquet_builder', false, 'Create Your Own Bouquet'),
  ('delivery_method_bloom_partner', false, 'Bloom Delivery Partner'),
  ('delivery_method_third_party_courier', false, 'Third-Party Courier');

-- PAYOUT LEDGER
create table public.florist_bank_accounts (
  id uuid primary key default gen_random_uuid(),
  florist_id uuid references public.florists(id) not null unique,
  account_holder_name text not null, account_number_last4 text not null,
  account_number_encrypted text not null, ifsc_code text not null, upi_id text,
  is_verified boolean default false, verified_at timestamptz, created_at timestamptz default now()
);

create table public.ledger_entries (
  id uuid primary key default gen_random_uuid(),
  florist_id uuid references public.florists(id) not null,
  order_id uuid references public.orders(id),
  entry_type text not null check (entry_type in ('order_earning','cod_commission_due','refund_clawback','payout','manual_adjustment')),
  amount numeric not null, direction text not null check (direction in ('credit','debit')),
  note text, created_by uuid references public.profiles(id),
  payout_batch_id uuid, created_at timestamptz default now()
);
create rule ledger_no_update as on update to public.ledger_entries do instead nothing;
create rule ledger_no_delete as on delete to public.ledger_entries do instead nothing;

create table public.payout_batches (
  id uuid primary key default gen_random_uuid(),
  florist_id uuid references public.florists(id) not null,
  period_start timestamptz not null, period_end timestamptz not null,
  total_amount numeric not null,
  status text default 'pending_review' check (status in ('pending_review','approved','processing','paid','failed','rejected')),
  approved_by uuid references public.profiles(id), approved_at timestamptz,
  payout_reference text, paid_at timestamptz, failure_reason text,
  created_at timestamptz default now()
);

create table public.payout_batch_items (
  id uuid primary key default gen_random_uuid(),
  payout_batch_id uuid references public.payout_batches(id) not null,
  ledger_entry_id uuid references public.ledger_entries(id) not null unique,
  amount numeric not null
);

-- DEVICE TOKENS (push)
create table public.device_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references public.profiles(id) not null,
  fcm_token text not null, device_platform text, created_at timestamptz default now()
);
```

**Core functions to build (logic already proven earlier in this project — reimplement identically):**
- `record_order_earning()` — trigger on order delivered, branches prepaid-credit vs COD-debit, per the itemized pricing rules (FR-32 to FR-34)
- `record_refund_clawback()` — trigger on refund
- `florist_pending_balance()`, `florist_eligible_balance()`, `florist_available_balance()`
- `generate_payout_batch()`, `generate_all_payout_batches()`
- `approve_payout_batch()`, `reject_payout_batch()`, `mark_payout_batch_paid()`, `record_manual_adjustment()` — all admin-gated with `has_role()` checks
- `nearby_shops()` — PostGIS radius query
- `agent_mark_delivered()` — agent-scoped, checked against `delivery_agents.user_id`
- `link_delivery_agent()` — shown above

**RLS:** apply the complete, already-proven policy set from earlier in this project to every table above — customer/florist/agent/admin scoping exactly as previously established, using `(select auth.uid())` wrapping from day one this time (not retrofitted later).

---

## 7. Native Android Implementation Notes

- **Networking:** Retrofit + OkHttp, calling Supabase's REST/RPC/Auth/Functions endpoints directly over HTTPS (see the earlier native Android guide for the concrete setup).
- **Repository pattern:** one Java class per domain (`AuthRepository`, `ShopRepository`, `OrderRepository`, `PayoutRepository`, `DeliveryAgentRepository`, etc.), each wrapping the relevant Retrofit calls — mirrors the same clean separation used in the earlier Flutter/repository designs in this project.
- **Two app modules:** consider a single Android Studio project with two separate app modules (`customer-app`, `partner-app`) sharing a common `core` module for the networking/models layer — avoids duplicating the Supabase client setup twice.
- **Partner app role-branching:** on login, after `link_delivery_agent()` check and `has_role()` check, load either the Owner navigation graph or the Agent navigation graph — two distinct sets of Activities/Fragments within the same app.

---

## 8. Screen Inventory

**Customer App:** Splash, Role Select, OTP Login, Home (nearby shops), Manual Location, Shop Detail, Product Detail, Cart, Checkout (Address/Slot/Payment), Order Confirmation, Order Tracking, Order History, Profile, Reviews, Support.

**Bloom Partner App — Owner view:** Shop Setup, Bank Setup, Dashboard, Order Detail/Action, Order History, Delivery Team (add/manage agents), Inventory, Add/Edit Product, Add-ons, Coupons, Earnings, Payout Batch Detail, Support, Notifications, Profile.

**Bloom Partner App — Agent view:** Agent Home (on-duty toggle + today's deliveries), Delivery Detail (navigate + mark delivered), Delivery History, Agent Profile.

**Admin Console (web):** Approvals Queue, Analytics, Disputes, Feature Flags, Payout Batch Review.

---

## 9. Development Phases

| Phase | Deliverable |
|---|---|
| 0 | New Supabase project, full schema + RLS from Section 6 |
| 1 | Customer App: Android Studio setup, Retrofit/Auth wiring, role-based login |
| 2 | Customer App: nearby shops, browsing, cart, checkout, Razorpay (server-verified) |
| 3 | Customer App: order tracking (Realtime), history, reviews |
| 4 | Partner App: Owner onboarding, dashboard, order actions, inventory |
| 5 | Partner App: Delivery Agent invite/link flow + Agent view (4 screens) |
| 6 | Payout ledger: triggers, balance functions, batch generation |
| 7 | Admin Console (web): approvals, payout review, feature flags, disputes |
| 8 | Push notifications (FCM) across both apps |
| 9 | Full regression via Acceptance Test Script (Section 10) |
| 10 | Production hardening: environment separation, crash reporting, legal docs, store submission |

---

## 10. Acceptance Test Script

1. Florist signs up → shop setup → bank setup → Dashboard
2. Florist adds a product → `pending` → invisible to customers
3. Florist adds a Delivery Agent (name + phone)
4. Agent installs Partner app on their own phone, signs up with that same phone number → automatically lands in Agent view, sees zero deliveries yet
5. Admin approves florist + product
6. Customer discovers shop via GPS, orders, pays (Razorpay test mode)
7. Florist accepts order, assigns it to the Agent
8. Agent sees the delivery appear on their Agent Home, taps Navigate, taps Mark Delivered
9. Agent attempts (via direct API call, not the UI) to mark a different florist's order delivered — confirm it's rejected server-side, not just hidden in the UI
10. Customer sees live tracking update to Delivered without refreshing
11. Customer leaves a review
12. Weekly batch runs → florist's earning appears as a pending payout batch
13. Admin approves, marks paid with a reference
14. Florist's Earnings screen reflects the payout in history, balance returns to zero for that batch
15. Florist deactivates the Agent → confirm the agent can no longer see new incoming deliveries, but their history remains intact

If all 15 steps pass without manual database intervention, the rebuild is functionally complete.
