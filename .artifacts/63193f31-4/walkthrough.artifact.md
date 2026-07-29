# Walkthrough - Navigation Framework & UX Polish

I have implemented a more robust architectural framework by transitioning to a Fragment-based navigation system and addressed several UX and security issues.

## Changes Made

### 1. Architectural Transition (Fragments)
- **Persistent Navigation**: Moved the core sections of the app (**Home**, **Orders**, and **Account**) from standalone Activities into **Fragments** (`HomeFragment`, `OrdersFragment`, `ProfileFragment`).
- **Main Container**: Updated `HomeActivity` to act as the primary host, using a `BottomNavigationView` to switch between these fragments seamlessly while keeping the navigation bar visible.
- **Improved Lifecycle**: Using Fragments allows the app to maintain state better when navigating between the main sections.

### 2. Security & Session Reliability
- **JWT Expiration Handling**: Enhanced the `RetrofitClient` with an automatic **401 Unauthorized** interceptor. If a user's session expires (JWT expired), the app will now automatically clear the local session and redirect them to the Login screen, preventing "stale" errors.
- **Splash Cleanup**: Refined the initial session check in `SplashActivity` to ensure a clean handoff to either the Home or Login flow.

### 3. UI Refinements & Polish
- **Manual Location UI**: Redesigned the `ManualLocationActivity` layout. The input field is now at the top, followed immediately by the "Set Location" button for a more intuitive, top-down workflow.
- **Icon Uniformity**: Updated all back navigation icons to solid **Black (#000000)** across every screen (Shop Detail, Product, Cart, etc.) for better contrast against the brand background.
- **Cart Access**: Integrated a **Cart Icon** into the Home screen toolbar, providing a quick shortcut to the checkout process.
- **Empty State Banners**: Standardized the "Service coming soon" banner to appear whenever a location search yields no results.

## Design Principles & Patterns Used
- **Single Activity Multiple Fragments**: Adopted this modern Android pattern to improve navigation speed and maintain UI consistency.
- **Interceptor Pattern**: Used to centralize auth error handling (401), decoupling it from individual API calls.
- **Constraint-Based Layouts**: Refined XML layouts to ensure buttons and inputs are always accessible and correctly positioned.

## Verification Results
- **Build Status**: Successfully performed a clean build (`assembleDebug`).
- **Navigation Flow**: Verified that the Bottom Navigation Bar persists across Home, Orders, and Profile sections.
- **Session Flow**: Confirmed that the logout logic in the Profile correctly resets the app state.

> [!TIP]
> Use these coordinates in the manual location search to see your shops:
> - **Mumbai**: `19.0596, 72.8258`
> - **Delhi**: `28.6328, 77.2167`
> - **Bangalore**: `12.9716, 77.6412`
