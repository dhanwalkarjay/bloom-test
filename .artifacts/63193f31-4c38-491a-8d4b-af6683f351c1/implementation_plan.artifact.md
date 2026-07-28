# Implementation Plan - Profile & Account Management

This plan implements the User Profile screen, allowing users to view their details, manage saved addresses, and securely log out.

## Proposed Changes

### 1. Data Layer (`data/api` & `data/repository`)

#### [MODIFY] [SupabaseAPI.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/data/api/SupabaseAPI.java)
- Add `getProfile` endpoint to fetch the user's details from the `profiles` table.
  ```java
  @GET(Constants.REST_ENDPOINT + "profiles")
  Call<List<Profile>> getProfile(@Query("id") String id);
  ```

#### [NEW] [ProfileRepository.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/data/repository/ProfileRepository.java)
- Implement `getProfile(userId)` using the API call.
- Handles the state via `NetworkResult<Profile>`.

### 2. UI Layer (`ui/profile`)

#### [NEW] [activity_profile.xml](file:///E:/projects/New%20Idea/Bloom/app/src/main/res/layout/activity_profile.xml)
- Header with user name and phone number.
- Section for "Saved Addresses" with a `RecyclerView`.
- "Logout" button with clear visual distinction (Bloom Pink).

#### [NEW] [ProfileActivity.java](file:///E:/projects/New%20Idea/Bloom/app/src/main/java/com/bloom/customer/ui/profile/ProfileActivity.java)
- Fetch and display profile data on load.
- Re-use `AddressRepository` to populate the addresses list.
- **Logout Logic**: Clears `SessionManager`, finishes all activities, and returns to `LoginActivity`.

## Verification Plan

### Manual Verification
1. **Data Display**: Launch the Profile screen and verify that your `full_name` and `phone` are correctly pulled from Supabase.
2. **Address List**: Verify that your saved addresses appear in the list within the profile screen.
3. **Logout**: Click the Logout button. Verify that the app returns to the Login screen and that reopening the app shows the Login screen (session cleared).
