# Walkthrough - Profile & Account Management

I have successfully implemented the User Profile screen, enabling users to view their account details, manage their saved addresses, and securely log out of the application.

## Changes Made

### 1. Data Layer Enhancements
- **[SupabaseAPI.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/data/api/SupabaseAPI.java)**: Added the `getProfile` endpoint to fetch user details from the `profiles` table.
- **[ProfileRepository.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/data/repository/ProfileRepository.java)**: Created a new repository to handle profile data retrieval and state management.

### 2. Profile UI
- **[activity_profile.xml](file:///E:/projects/New%20Idea/Bloom/app/src/main/res/layout/activity_profile.xml)**: Built a clean, professional layout featuring:
    - User avatar and basic information (Name & Phone).
    - An integrated list of **Saved Addresses** reusing the existing address UI components.
    - A clear, Pink-themed **Logout** button.
- **[ProfileActivity.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/ui/profile/ProfileActivity.java)**:
    - Orchestrates the simultaneous loading of profile data and saved addresses.
    - Implemented secure **Logout logic** that clears the `SessionManager` and returns the user to the Login screen, ensuring no stale sessions remain.

### 3. Manifest Integration
- **[AndroidManifest.xml](file:///E:/projects/New%20Idea/Bloom/app/src/main/AndroidManifest.xml)**: Registered the `ProfileActivity` and ensured it follows the consistent theme and package structure.

## Design Principles & Patterns Used
- **SOLID - Single Responsibility**: `ProfileActivity` focus is strictly on account management, while data retrieval is handled by the respective repositories.
- **Code Reuse**: Efficiently reused `AddressRepository` and `AddressAdapter` to maintain consistency across the app.
- **Secure Session Termination**: Logout logic uses `finishAffinity()` and `FLAG_ACTIVITY_CLEAR_TASK` to ensure the user cannot return to authenticated screens after logging out.

## Verification Results
- **Build Status**: Successfully performed `assembleDebug`.
- **Data Binding**: Verified that user profile fields and address lists correctly populate from the Supabase response.
- **Session Safety**: Confirmed that `clearSession()` is called during logout, protecting user data.

> [!TIP]
> You can now link the Profile screen from your Home toolbar to allow users easy access to their account settings.
