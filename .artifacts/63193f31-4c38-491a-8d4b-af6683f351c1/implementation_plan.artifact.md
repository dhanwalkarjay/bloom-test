# Implementation Plan - Navigation Framework & UX Polish

This plan addresses the persistent navigation bar, JWT expiration handling, and several UI refinements to ensure a professional and functional user experience.

## User Review Required

> [!IMPORTANT]
> To keep the bottom navigation bar persistent, I am moving the Home, Orders, and Profile sections into **Fragments**. `HomeActivity` will become the primary container for these fragments.

## Proposed Changes

### 1. Navigation Framework (Fragments)
- **[NEW] HomeFragment.java**, **OrdersFragment.java**, **ProfileFragment.java**: Migrate the logic from the respective Activities into these Fragments.
- **[MODIFY] [HomeActivity.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/ui/home/HomeActivity.java)**:
    - Act as the main container for the fragments.
    - Handle Bottom Navigation switching via `FragmentManager`.
- **[MODIFY] [activity_home.xml](file:///E:/projects/New%20Idea/Bloom/app/src/main/res/layout/activity_home.xml)**: Add a `FragmentContainerView` for hosting sections.

### 2. JWT & Security
- **[MODIFY] [RetrofitClient.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/data/api/RetrofitClient.java)**:
    - Handle `401 JWT Expired` errors by clearing the session and redirecting the user to `LoginActivity`.
- **[MODIFY] [SplashActivity.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/ui/splash/SplashActivity.java)**: Ensure a clean check of the session before routing.

### 3. UI Polish
- **[MODIFY] [activity_manual_location.xml](file:///E:/projects/New%20Idea/Bloom/app/src/main/res/layout/activity_manual_location.xml)**:
    - Move the input field to the top.
    - Place the "Set Location" button directly below it.
- **Back Icons**: Set `app:tint="#000000"` for all back navigation icons to ensure they are black.
- **Empty States**: Explicitly show the "Service coming soon!" banner if a city search returns zero shops.

## Verification Plan

### Manual Verification
1. **JWT Expiration**: Simulate a 401 error and verify the app returns to Login.
2. **Bottom Nav**: Switch between Home, Orders, and Profile. Verify the navbar stays visible and the state is preserved.
3. **Manual Entry**: Open manual location, verify the new top-down layout, enter a city, and confirm it refreshes the shop list.
4. **Icons**: Check all screens (Shop Detail, Product, Cart) to ensure back icons are black.

### Sample Testing Coordinates
To see products in your DB, use these locations in manual entry:
- **Mumbai**: `19.0596, 72.8258`
- **Delhi**: `28.6328, 77.2167`
- **Bangalore**: `12.9716, 77.6412`
