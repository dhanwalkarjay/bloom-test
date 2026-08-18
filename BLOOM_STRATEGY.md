# 🌸 BLOOM — Complete Business Strategy & Product Roadmap
> Created: August 2026 | By: Bloom Founding Team
> This document covers everything from the business idea, market analysis, identified problems, zero-budget solutions, and the complete development build order.

---

## 1. 🎯 What is Bloom?

**Bloom** is a hyper-local, on-demand flower and gifting delivery platform — essentially "Zomato/Swiggy for Bouquets."

It connects premium customers with local florists in their city, enabling them to order flowers, bouquets, and gift combos online — with scheduled delivery, live order tracking, and a post-purchase review experience.

### The Core Motive
> People don't buy flowers. They buy the **feeling of being a good person**. They buy the **relief of not forgetting**. They buy the **certainty that someone will feel loved**.

Bloom solves the real-world problem of going somewhere empty-handed — a birthday, a wedding, a hospital visit, a surprise — by making a premium gifting experience available at their fingertips, wherever they are.

---

## 2. 📦 The Complete System (4 Parts)

| App | Who uses it | Purpose |
|---|---|---|
| **Customer App** (Android) | End customers | Discover florists, order flowers, track delivery, rate experience |
| **Merchant/Florist App** (Android) | Shop owners | Manage orders, stock, deliveries |
| **Delivery Agent App** (Android) | Shop's delivery staff | Accept delivery tasks, navigate, confirm delivery |
| **Admin Panel** (Web) | Bloom team only | Manage all shops, orders, disputes, analytics |

> **Note**: All 4 parts connect to the same Supabase backend.

---

## 3. 🔍 Market Analysis

- The Indian gifting market is **₹3.7 lakh crore** and growing at 12% CAGR.
- Flowers are the #1 impulse gift.
- Zomato, Swiggy, BlinkIt **do NOT serve the floral gifting segment** properly.
- Hyper-local focus is critical — flowers are perishable, logistics must be tight.
- Occasions-driven buying (birthdays, anniversaries, Valentine's Day, weddings) creates predictable, plannable demand spikes.

### Competitive Gap
No existing Indian app offers:
- Premium florist discovery by location
- Scheduled delivery for specific occasions
- Gifting addons (chocolates, vases) from the same florist
- Live order tracking for flower deliveries

**This is Bloom's market to own.**

---

## 4. 💡 The Complete User Journey (App Flow)

### Step 1 — Identity & Security
`SplashActivity` → `LoginActivity` / `SignupActivity` → `OtpVerifyActivity`

- Supabase Auth handles identity with OTP-based verification.
- Session tokens stored securely in `SessionManager`.

### Step 2 — Discovery & Curation
`HomeFragment` / `ExploreFragment` / `LuxFragment`

- Shows florists and products **only in the user's current location or entered location**.
- Featured products appear in horizontal carousels.
- Occasion-based discovery (Birthday, Wedding, Anniversary, etc.)
- **Bloom Lux** is a dedicated premium tier for VIP users.

### Step 3 — Storefront & Upselling
`ShopDetailActivity` → `ProductDetailActivity`

- Users browse the florist's full inventory.
- Addons (chocolates, vases, etc.) from the **same shop** are shown on product pages to increase Average Order Value.
- Gifts and bouquets come from the same shop only — no multi-vendor cart complexity.

### Step 4 — Checkout Funnel
`CartActivity` → `AddressSelectActivity` → `DeliverySlotActivity` → `PaymentActivity`

- Cart is a global single-source-of-truth via `CartRepository`.
- Coupon validation for discounts.
- Delivery scheduling for specific dates/times (critical for gifting).
- Payment supports UPI (GPay, PhonePe), ATM/Card, COD.

### Step 5 — Fulfillment & Loyalty
`OrderTrackingActivity` → `RateOrderActivity` / `ReviewActivity`

- **Live tracking** via Supabase Realtime (WebSockets) — no refresh needed.
- Status updates: Placed → Confirmed → Preparing → Out for Delivery → Delivered.
- Post-delivery rating for the product and the florist.
- Photo-based proof of delivery and freshness complaints.

---

## 5. ⚠️ Identified Problems & Zero-Budget Solutions

### 🔴 Problem 1: The Chicken-and-Egg Problem (Most Critical)
**Problem**: You need shops before customers and customers before shops. Classic marketplace deadlock.

**Zero-Budget Solution — 3 Steps:**

**Step A — Go to shops first. Make them feel like they can't lose.**
> Walk into 5–10 florists and say: *"We are building an app like Swiggy but for flowers. Listing is FREE. Zero commission for 3 months. If no orders come, you lost nothing."*

Most florists have zero online presence. You're giving them free marketing. They will say yes.
**Cost: ₹0. What you need: Courage to walk in.**

**Step B — "Wizard of Oz" Testing (Fake it till you make it)**
Before your merchant app is built, manually process orders:
- Set up a WhatsApp Business number or Google Form for ordering.
- When an order comes, call the florist directly, coordinate delivery manually.
- Track everything in a spreadsheet.

The customer thinks they're using a product. You're running it manually behind the scenes. This gives you real data with zero infrastructure.
**Cost: ₹0. What you need: A few hours per day.**

**Step C — The College Campus Hack**
As students, you have direct access to thousands of users.
- Post on Instagram/WhatsApp: *"Order flowers on campus for birthdays. Delivered to your hostel in 2 hours."*
- Partner with ONE florist near your college.
- Target college festivals, Freshers' Night, Valentine's Day, Teachers' Day.

**Cost: ₹0. What you need: One Instagram post.**

---

### 🔴 Problem 2: Freshness & Trust
**Problem**: Flowers die in hours. One wilted bouquet destroys trust permanently.

**Solution — Merchant SLA (Service Level Agreement)**
Before onboarding any shop, make them commit to 3 rules:
1. Orders older than 24 hours cannot be dispatched.
2. If a customer complains with a photo, the shop must re-deliver or gets suspended.
3. Shop must mark items as "Out of Stock" within 30 minutes of running out.

Compliance is tied to their star rating. Below 4.0 stars = warning. Repeated issues = suspended.

Build a **"Freshness Verified"** badge for compliant shops in the app.
**Cost: ₹0. What you need: A Google Doc as the agreement.**

---

### 🔴 Problem 3: Real-Time Inventory
**Problem**: Shops run out of stock mid-day. Customers order, pay, then get told "not available."

**Short-term (Zero Budget) Solution:**
- Create a WhatsApp group for all merchant partners.
- Every morning: *"Please reply with today's available stock and any sold-out items."*
- Manually update Supabase with availability.

This doesn't scale to 1,000 shops — but you don't have 1,000 shops yet.

**Long-term (When Merchant App is Built):**
- Add a one-tap **"Mark as Sold Out"** button in the merchant app.
- This is a 2-hour engineering task with massive business impact.

---

### 🔴 Problem 4: Demand Spikes (Valentine's Day, etc.)
**Problem**: Valentine's Day could be 50x normal volume. System crashes = destroyed reputation.

**Solution:**
- Supabase (your backend) auto-scales. You're protected on the database side.
- Limit orders per shop per day during peak seasons (merchant app setting).
- Do load testing on the customer app before every major occasion.
- Communicate proactively: *"Pre-book your Valentine's Day flowers now."* This spreads demand over days instead of one hour.

---

### 🟡 Problem 5: Delivery Quality Control
**Problem**: You have no control over the shop's delivery guy. A bad experience on an anniversary is an emotional disaster.

**Solution:**
- Create a **"Bloom Partner Standards"** one-page document. Shops must follow it to remain listed.
- Live delivery tracking (already built in `OrderTrackingActivity`) — customers can see exactly where their order is.
- Strict rating system — delivery experience is rated separately from product quality.
- Delivery agent must call recipient before arriving (add this to the standards document).

---

### 🟡 Problem 6: No Florist Nearby — Dead End Screen
**Problem**: User opens app in a new city, sees empty screen, leaves forever.

**Solution:**
- When no local shop is found, show an **"Expand Search"** option.
- Show shops that ship nationally (partner with 1-2 large florists like FNP as fallback).
- Add a **"Notify me when Bloom arrives here"** button. This gives you demand signals for which cities to expand to next.

Never show an empty dead-end screen. Always show something.

---

### 🟡 Problem 7: Customer Decision Paralysis
**Problem**: 5 florists in one area — how does a customer choose? They abandon without a clear signal.

**Solution — Occasion Tags & Shop Attributes:**
Tag shops with: `Best for Weddings`, `Budget-Friendly`, `Premium/Luxury`, `Same-Day Delivery`, `Corporate Gifting`.

These trigger fast emotional decision-making. Users stop thinking and start clicking.

This is a pure design + database tag work — no new backend required. Add occasion tags to your Supabase product table and display them in the UI.

---

## 6. 🏗️ Build Order & Roadmap

### The Core Principle
> Build in the order that gets you to your **first real order** the fastest. Do not wait for perfection.

```
Phase 1 (Now)          Phase 2 (Next)         Phase 3 (Later)
─────────────────      ─────────────────      ─────────────────
Customer App      ──►  Merchant App       ──►  Admin Panel
(Already ~80%)         (4 features only)       (Supabase dashboard
                                                works for now)
                                          ──►  Delivery Agent App
                                               (WhatsApp works for
                                                first 50 orders)
```

---

### Phase 1 — Customer App (Finish & Polish)

**Goal**: A customer can open the app, find a nearby shop, add to cart, pay, and track their order.

Checklist of what needs to be working end-to-end:
- [ ] Auth flow (Login, Signup, OTP)
- [ ] Location-based shop discovery
- [ ] Shop detail with product grid
- [ ] Product detail with addon upsell
- [ ] Cart management
- [ ] Address selection & delivery slot booking
- [ ] Payment (UPI, Card, COD)
- [ ] Order confirmation screen
- [ ] Live order tracking
- [ ] Rate & review post-delivery

---

### Phase 2 — Merchant/Florist App (4 Features Only — No More)

**Goal**: A florist can receive orders, accept them, and mark them as delivered. Nothing more in V1.

| # | Feature | Complexity |
|---|---|---|
| 1 | See incoming orders (with customer details, items, address) | Low |
| 2 | Accept or Reject an order | Low |
| 3 | Mark order as Ready / Out for Delivery | Low |
| 4 | Mark stock items as Available / Sold Out | Low |

**Do NOT build in V1**: Analytics dashboards, revenue reports, inventory management, multiple staff logins. Build those only when shops ask for them.

---

### Phase 3 — Admin Panel (Web) & Delivery Agent App

Build these **when you have the problem of too many orders to manage manually.** That is a good problem to have.

Admin Panel needs (V1):
- View all orders across all shops
- View all registered shops and customers
- Suspend/approve shops
- Handle customer complaints manually

Delivery Agent App needs (V1):
- See assigned delivery
- Get customer address (with maps link)
- Mark as Delivered with photo proof

---

## 7. 📅 Realistic 90-Day Zero-Budget Launch Plan

| Week | Action | Cost |
|---|---|---|
| Week 1-2 | Polish and test customer app end-to-end | ₹0 |
| Week 3-4 | Physically visit 5-8 local florists. Show them the working app. Onboard 2-3. | ₹0 |
| Week 5-6 | Launch a WhatsApp/Instagram campaign in your college. Process first orders manually (Wizard of Oz). | ₹0 |
| Week 7-8 | Release customer app to a small group. Fix real-world problems as they appear. | ₹0 |
| Week 9-10 | Collect reviews, fix problems, get word-of-mouth referrals from happy shops and customers. | ₹0 |
| Week 11-12 | Start building merchant app. | ₹0 |
| Month 3+ | Start charging 8-12% commission per order from shops. | First Revenue |

---

## 8. 💰 Business Model

| Revenue Stream | When to Activate |
|---|---|
| Commission (8-12% per order) | After proving value to merchants (~Month 3) |
| Featured Shop Listings | Once you have multiple shops competing for visibility |
| Merchant Subscription Plan | Once shops want analytics, priority support, etc. |
| Bloom Lux Premium Tier | Corporate accounts, exclusive arrangements |
| Surge Pricing on peak dates | Valentine's Day, Mother's Day, etc. |

---

## 9. 🧠 Key Psychological Insights

- **The emotional purchase**: People pay for convenience and certainty when it comes to flowers, not the lowest price. Price sensitivity is low.
- **Occasion anxiety**: The fear of going somewhere empty-handed is the #1 emotional driver for downloading Bloom. Market to this pain.
- **The "good person" feeling**: Your app should sell this at every touchpoint. Home screen: *"Don't let them down."* Order confirmation: *"Your love is on its way 🌸."*
- **Scheduled delivery trust**: Promising delivery at 9 AM sharp on a birthday removes the anxiety of forgetting. This feature is a massive trust builder.
- **Review loop**: Happy florists refer other florists. Happy customers refer other customers. The review and rating system is not just quality control — it is your growth engine.

---

## 10. ⚡ The Most Important Mindset

> **Do NOT try to build a platform first. Build a habit first.**

Zomato's first 1,000 users didn't use an app. They called a phone number. The app came later.

Your first 1,000 orders don't need a perfect merchant app, a delivery tracking system, or an admin panel. They need **one florist, one working customer app, and you personally making sure every order is perfect.**

Once you have 100 orders per month, florists will beg to join. The chicken-and-egg problem solves itself when you prove demand exists.

---

## 11. 🗺️ Launch City Strategy

**Recommended starting cities**: Nashik or Nagpur (Tier 2)

**Why Tier 2 over Tier 1 (Mumbai/Pune):**
- Local florists have zero digital presence — they are grateful for the opportunity.
- Lower customer expectations for delivery speed (room to learn and fix mistakes).
- Less competition from well-funded startups.
- Manageable geography — can physically visit all shops in 2-3 days.
- Once proven here, the exact same playbook works in every other Tier 2 city in India.

**Expansion Rule**: Only expand to the next city after achieving **100 orders/month** consistently in the current city.

---

## 12. 🛠️ Tech Stack Summary

| Layer | Technology |
|---|---|
| Customer App | Android (Java), ViewBinding, MVVM |
| Merchant App | Android (Java or Kotlin) |
| Backend | Supabase (PostgreSQL + Realtime + Auth + Storage) |
| API Layer | Retrofit + Supabase REST API |
| Real-time Tracking | Supabase Realtime (WebSockets) |
| Admin Panel | Web (React or simple HTML dashboard) |
| Payments | Razorpay / UPI Intent / COD |

---

*This document is a living strategy guide. Update it as you learn from real customers and florists.*
*Last updated: August 2026*
