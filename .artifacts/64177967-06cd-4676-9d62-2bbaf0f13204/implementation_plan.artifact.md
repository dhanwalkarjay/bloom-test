# Fix Duplicate Resources Error

The build is failing because `Theme.Bloom` is defined in both `res/values/styles.xml` and `res/values/themes.xml`.

## Proposed Changes

### [res/values]

#### [MODIFY] [themes.xml](file:///E:/projects/New Idea/Bloom/app/src/main/res/values/themes.xml)
- Update `Theme.Bloom` and `Base.Theme.Bloom` to match the design defined in `styles.xml`.
- Ensure it uses `Theme.MaterialComponents.Light.NoActionBar` as the parent (as per the current working design) or decide if we should transition to Material 3.
- Given the existing `styles.xml` content, I will stick to `MaterialComponents` for now to avoid breaking UI that depends on M2 behaviors.

#### [MODIFY] [styles.xml](file:///E:/projects/New Idea/Bloom/app/src/main/res/values/styles.xml)
- Remove the `Theme.Bloom` definition from this file to resolve the duplication.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:packageDebugResources` to verify that the duplicate resources error is resolved.
- Run `./gradlew :app:assembleDebug` to ensure the project builds successfully.

### Manual Verification
- Deploy the app to a device/emulator to ensure the theme is correctly applied.
