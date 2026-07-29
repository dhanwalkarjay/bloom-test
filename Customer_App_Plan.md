# Bloom Customer App — Complete Plan
### Native Android (Java) — Requirements, System Design, Folder Structure

Scope: this document covers **only the Customer-facing app**. The Bloom Partner app (Florist/Agent) and Admin Console are separate efforts, built later, sharing the same Supabase backend.

---

## 1. Vision

A native Android app where customers discover nearby open flower shops by GPS, browse and customize bouquets, check out, track delivery in real time, and review their order — with genuine native scroll/animation feel, not a wrapped web app.

---

## 2. Functional Requirements

### Authentication
- **FR-1:** Phone number + password login. OTP is required only once, at first signup, to verify the phone number.
- **FR-2:** New users complete a lightweight signup (name, phone, password) before OTP verification.
- **FR-3:** Session persists across app restarts (stored access/refresh tokens); auto-refresh silently when the access token expires.
- **FR-4:** "Forgot password" flow via recovery OTP.

### Location & Discovery
- **FR-5:** On Home screen load, request device location permission.
- **FR-6:** If granted, fetch GPS coordinates and query nearby shops (via the `nearby_shops` RPC).
- **FR-7:** If denied, show a manual location entry screen (search/select an area); allow re-requesting GPS later from Profile.
- **FR-8:** Shops list sorts: open-now first, then by distance, then by rating.
- **FR-9:** Closed shops remain visible, shown with reduced visual prominence and an "opens at X" indicator if known.
- **FR-10:** A secondary "Explore" tab allows browsing all open shops by occasion/category, independent of proximity.

### Shop & Product Browsing
- **FR-11:** Tapping a shop shows only that shop's product catalog.
- **FR-12:** Products are filterable by occasion, color, and budget.
- **FR-13:** Product detail shows customization options limited to what that specific product offers (size/wrap/ribbon/message card).
- **FR-14:** A "Create Your Own Bouquet" entry point is shown but disabled/"Coming Soon," driven by the `feature_flags` table, not hardcoded.
- **FR-15:** Add-ons and any active coupons are scoped to the specific shop being viewed.
- **FR-16:** LUX-tier products are visually distinguished (different card treatment) within the shop's catalog.

### Cart & Checkout
- **FR-17:** Cart holds items from a single shop only; attempting to add an item from a different shop prompts the user to clear the current cart first.
- **FR-18:** Checkout shows an itemized price breakdown: bouquet subtotal, add-ons, delivery fee, platform fee, tax, discount (if a coupon applied), total.
- **FR-19:** Delivery address selection: pick a saved address or add a new one (with map pin or manual entry).
- **FR-20:** Delivery timing selection: Instant / Same-Day / Scheduled are functional; Midnight is shown disabled per its feature flag.
- **FR-21:** Delivery fee is calculated by real distance between shop and delivery address at the point of address selection, and locked in for checkout.
- **FR-22:** Payment via Razorpay (order creation + signature verification both happen server-side via Edge Functions) or Cash on Delivery, where offered.
- **FR-23:** An order is only created in the database after payment is verified as successful (or immediately, for COD).

### Order Tracking & History
- **FR-24:** Order confirmation screen shown immediately after successful checkout.
- **FR-25:** Order tracking screen shows a live status timeline (Placed → Confirmed → Preparing → Out for Delivery → Delivered), updating in real time via Supabase Realtime without manual refresh.
- **FR-26:** Order history lists past orders, filterable by status, with a "Reorder" shortcut.
- **FR-27:** Reviews can be left only on orders marked `delivered`, tied to that specific order.

### Profile & Support
- **FR-28:** Profile screen: name, phone, saved addresses, language/currency preference, logout, account deletion.
- **FR-29:** Support screen: FAQ/help topics and a way to raise a support ticket tied to a specific order if applicable.

---

## 3. Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-1 | Genuine native UI — no WebView; native scroll physics, transitions, and gestures throughout. |
| NFR-2 | Nearby-shops results load in under 1.5 seconds on a typical mobile connection. |
| NFR-3 | Every write operation (order placement, review submission, address changes) is enforced server-side via Supabase RLS — the app never assumes client-side validation is sufficient. |
| NFR-4 | Payment flow never exposes the Razorpay secret key or trusts client-reported payment success. |
| NFR-5 | Location is fetched once per session/foreground event, not continuously polled, to preserve battery. |
| NFR-6 | The app functions (with reduced capability) when location permission is denied — never a hard crash or dead-end screen. |
| NFR-7 | Images are cached locally (not re-fetched on every screen revisit) to reduce data usage and improve perceived speed. |
| NFR-8 | Auth tokens are stored using Android's `EncryptedSharedPreferences`, never plain `SharedPreferences`. |
| NFR-9 | The app handles offline/poor connectivity gracefully — visible loading states, retry options, no silent failures. |

---

## 4. System Architecture

```
┌────────────────────────────────────────────────────────┐
│                     Customer App (Java)                   │
│                                                            │
│  UI Layer (Activities/Fragments + RecyclerView)            │
│         │                                                  │
│  ViewModel Layer (AndroidX ViewModel + LiveData)            │
│         │                                                  │
│  Repository Layer (one class per domain)                   │
│         │                                                  │
│  Network Layer (Retrofit + OkHttp interceptor for auth)     │
└───────────────────────┬────────────────────────────────┘
                         │  HTTPS
                         ▼
┌────────────────────────────────────────────────────────┐
│                        Supabase                            │
│  Auth (phone OTP + password) · Postgres/PostGIS + RLS       │
│  Storage (product images) · Edge Functions (Razorpay)       │
│  Realtime (order status subscription)                       │
└────────────────────────────────────────────────────────┘
```

**Layering rules:**
- UI layer never talks to Retrofit directly — always through a ViewModel, which goes through a Repository.
- Repositories are the only place that know about Supabase's specific endpoint shapes — if an endpoint changes, only the repository changes, not every screen using it.
- Cart is held in a local, in-memory/`SharedPreferences`-backed repository (not synced to the server until checkout) since it's ephemeral per-device state.

---

## 5. Data Models (Java POJOs, matching Supabase tables)

- `User` (profiles)
- `Address`
- `Shop` (florists — customer-facing subset of fields: id, name, distance, isOpen, rating, prepTime)
- `Product`
- `Addon`
- `Coupon`
- `CartItem` (local-only, not a direct table mirror — product + quantity + customization + selected add-ons)
- `Order`, `OrderItem`, `OrderAddon`
- `Review`
- `FeatureFlag`

Each POJO includes a `fromJson`-equivalent (Gson handles this automatically if field names match the JSON keys — **use `@SerializedName("snake_case_column")` annotations** on every field, since Java naming convention is camelCase but Postgres columns are snake_case; this is the direct equivalent of the `fromJson`/`toJson` key-matching lesson learned earlier in this project, just handled via annotation instead of manual mapping).

---

## 6. Screen Inventory

| # | Screen | Notes |
|---|---|---|
| 1 | Splash | Session check, route accordingly |
| 2 | Signup (name/phone/password) | |
| 3 | OTP Verify | First-time only |
| 4 | Login (phone or email + password) | |
| 5 | Forgot Password | |
| 6 | Home | Nearby shops list, RecyclerView |
| 7 | Manual Location Entry | GPS-denied fallback |
| 8 | Explore | Global browse by occasion/category |
| 9 | Shop Detail | One shop's catalog |
| 10 | Product Detail | Customization options |
| 11 | Cart | |
| 12 | Address Select / Add Address | |
| 13 | Delivery Slot Selection | Midnight shown disabled |
| 14 | Payment | Razorpay / COD |
| 15 | Order Confirmation | |
| 16 | Order Tracking | Realtime subscription |
| 17 | Order History | |
| 18 | Reviews | Post-delivery only |
| 19 | Profile | |
| 20 | Support | |

---

## 7. Folder Structure (Android Studio, Java)

```
app/
└── src/main/java/com/bloom/customer/
    │
    ├── BloomApplication.java              // Application class - init Retrofit, session manager
    │
    ├── data/
    │   ├── api/
    │   │   ├── SupabaseApi.java           // Retrofit interface: all REST/RPC endpoints
    │   │   ├── SupabaseAuthApi.java       // Retrofit interface: auth-specific endpoints
    │   │   └── RetrofitClient.java        // Builds the Retrofit instance + auth interceptor
    │   │
    │   ├── model/
    │   │   ├── User.java
    │   │   ├── Address.java
    │   │   ├── Shop.java
    │   │   ├── Product.java
    │   │   ├── Addon.java
    │   │   ├── Coupon.java
    │   │   ├── CartItem.java
    │   │   ├── Order.java
    │   │   ├── OrderItem.java
    │   │   ├── OrderAddon.java
    │   │   ├── Review.java
    │   │   ├── FeatureFlag.java
    │   │   └── AuthResponse.java          // Wraps access_token/refresh_token/user
    │   │
    │   ├── repository/
    │   │   ├── AuthRepository.java
    │   │   ├── ShopRepository.java
    │   │   ├── ProductRepository.java
    │   │   ├── AddressRepository.java
    │   │   ├── CartRepository.java        // Local, not server-backed
    │   │   ├── OrderRepository.java
    │   │   ├── PaymentRepository.java     // Wraps Edge Function calls
    │   │   ├── ReviewRepository.java
    │   │   └── FeatureFlagRepository.java
    │   │
    │   └── local/
    │       ├── SessionManager.java        // EncryptedSharedPreferences wrapper for tokens
    │       └── LocationHelper.java        // GPS permission + fetch wrapper
    │
    ├── ui/
    │   ├── splash/
    │   │   └── SplashActivity.java
    │   │
    │   ├── auth/
    │   │   ├── SignupActivity.java
    │   │   ├── OtpVerifyActivity.java
    │   │   ├── LoginActivity.java
    │   │   ├── ForgotPasswordActivity.java
    │   │   └── AuthViewModel.java
    │   │
    │   ├── home/
    │   │   ├── HomeActivity.java
    │   │   ├── HomeViewModel.java
    │   │   └── ShopListAdapter.java
    │   │
    │   ├── location/
    │   │   └── ManualLocationActivity.java
    │   │
    │   ├── explore/
    │   │   ├── ExploreActivity.java
    │   │   └── ExploreViewModel.java
    │   │
    │   ├── shop/
    │   │   ├── ShopDetailActivity.java
    │   │   ├── ShopDetailViewModel.java
    │   │   └── ProductGridAdapter.java
    │   │
    │   ├── product/
    │   │   ├── ProductDetailActivity.java
    │   │   └── ProductDetailViewModel.java
    │   │
    │   ├── cart/
    │   │   ├── CartActivity.java
    │   │   ├── CartViewModel.java
    │   │   └── CartAdapter.java
    │   │
    │   ├── checkout/
    │   │   ├── AddressSelectActivity.java
    │   │   ├── AddAddressActivity.java
    │   │   ├── DeliverySlotActivity.java
    │   │   ├── PaymentActivity.java
    │   │   └── CheckoutViewModel.java
    │   │
    │   ├── orderconfirmation/
    │   │   └── OrderConfirmationActivity.java
    │   │
    │   ├── ordertracking/
    │   │   ├── OrderTrackingActivity.java
    │   │   └── OrderTrackingViewModel.java
    │   │
    │   ├── orderhistory/
    │   │   ├── OrderHistoryActivity.java
    │   │   ├── OrderHistoryViewModel.java
    │   │   └── OrderHistoryAdapter.java
    │   │
    │   ├── reviews/
    │   │   └── ReviewActivity.java
    │   │
    │   ├── profile/
    │   │   ├── ProfileActivity.java
    │   │   ├── EditProfileActivity.java
    │   │   └── SavedAddressesActivity.java
    │   │
    │   └── support/
    │       └── SupportActivity.java
    │
    └── util/
        ├── Constants.java                 // Base URL, anon key, feature flag keys
        ├── CurrencyFormatter.java
        ├── DateTimeUtil.java
        ├── ValidationUtil.java
        └── NetworkResult.java             // Generic wrapper: Loading / Success / Error states

app/src/main/res/
├── layout/                                // One XML layout per Activity/Fragment/list-item
├── drawable/
├── values/
│   ├── colors.xml                         // #E85D75, #4A7C59, #FFFBF7 etc.
│   ├── strings.xml
│   ├── styles.xml
│   └── dimens.xml
```

**Why this structure:**
- `data/` and `ui/` are cleanly separated — the networking/model layer knows nothing about Activities, and the UI layer never constructs a Retrofit call directly.
- Each screen gets its own package under `ui/`, keeping related files (Activity + ViewModel + Adapter) together rather than scattered across generic "activities/" and "adapters/" folders — easier to navigate as the app grows.
- `util/NetworkResult.java` is worth building early — a simple sealed-style wrapper (`Loading`, `Success<T>`, `Error`) that every repository method returns via `LiveData<NetworkResult<T>>`, so every screen handles loading/error states the same consistent way instead of ad-hoc null-checking.

---

## 8. Build Order (matches Phase 1-3 of the master roadmap)

1. Project setup: Gradle dependencies (Retrofit, OkHttp, Gson, Glide/Coil for images, EncryptedSharedPreferences, Google Play Services Location), `RetrofitClient` + `SessionManager` wired first, before any screen
2. Auth: Signup → OTP → Login, fully working end-to-end against Supabase, tested standalone before building anything else
3. Home + nearby shops (proves location + RPC calling works)
4. Shop Detail → Product Detail → Cart (proves the core browsing loop)
5. Checkout: Address → Delivery Slot → Payment (Razorpay test mode) → Confirmation
6. Order Tracking (Realtime) → Order History → Reviews
7. Profile, Support, polish pass (loading states, empty states, error handling per NFR-9)

---

## 9. Acceptance Test Script (Customer App only)

1. Signup with a real phone number → OTP → account created
2. Log out, log back in with phone/email + password → no OTP required
3. Grant location → Home shows real nearby shops sorted correctly
4. Deny location on a fresh install → manual entry fallback works
5. Browse a shop → customize a product → add an add-on → add to cart
6. Attempt to add a product from a different shop → confirm the single-vendor-cart prompt appears
7. Checkout: add address, select Same-Day delivery, confirm Midnight is visibly disabled
8. Pay via Razorpay test mode → order confirmation appears
9. Order tracking shows "Placed" and updates live as status changes (test this by manually updating the order's status in Supabase and confirming the app reflects it without refreshing)
10. Once delivered, leave a review → confirm it saves and appears
11. Order history shows the completed order; "Reorder" repopulates the cart correctly
