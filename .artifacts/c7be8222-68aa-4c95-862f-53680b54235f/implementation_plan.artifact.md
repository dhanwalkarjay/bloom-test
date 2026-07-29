# Restyle Home Screen Design

Update the Home screen (Activity and Fragment) to match the new Bloom design specifications, using existing styles and dimensions.

## Proposed Changes

### Navigation

#### [MODIFY] [bottom_nav_menu.xml](file:///E:/projects/New Idea/Bloom/app/src/main/res/menu/bottom_nav_menu.xml)
- Add two additional items to reach the required 5-icon bottom navigation bar.
- Items: Home, Search, Orders, Notifications, Profile.

### Home Activity

#### [MODIFY] [activity_home.xml](file:///E:/projects/New Idea/Bloom/app/src/main/res/layout/activity_home.xml)
- Update background to `@color/bloom_background`.
- Ensure `BottomNavigationView` is styled correctly and has no hardcoded colors.

### Home Fragment

#### [MODIFY] [fragment_home.xml](file:///E:/projects/New Idea/Bloom/app/src/main/res/layout/fragment_home.xml)
- **Sticky Header**: Redesign `AppBarLayout` with:
    - Location selector: `llLocation` containing `tvCurrentLocation` and a chevron icon.
    - Action icon: `ivCart` (ID preserved) displaying a notification bell icon.
- **Main Content**: Wrap in a `NestedScrollView` (inside `SwipeRefreshLayout`) with the following sections:
    - **Occasions**: Horizontal scrolling chip row ("Shop by Occasion").
    - **Hero Banner**: Full-width card promoting seasonal campaigns.
    - **LUX Banner**: Dark-and-gold promotional card using `Bloom.Card` and LUX colors.
    - **Bestsellers**: "Bestsellers" section header and a horizontal scrolling card carousel.
    - **Shops List**: Existing `rvShops` (RecyclerView) at the bottom.
- **Styling**:
    - Use `Bloom.Text.Headline/Title/Body/Caption` for all text.
    - Use `Bloom.Card` for all card elements.
    - Use `@dimen/spacing_m` for standard padding/margins.
    - No hardcoded colors; use `@color/*`.

## Verification Plan

### Automated Tests
- Build the project to ensure no XML errors.
- Verify that all IDs used in `HomeFragment.java` (`llLocation`, `tvCurrentLocation`, `ivCart`, `rvShops`, `swipeRefresh`, `progressBar`, `emptyState`) are preserved.

### Manual Verification
- Visual check of the layout structure in the IDE's layout editor.
