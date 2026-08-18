package com.bloom.customer.util;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bloom.R;

/**
 * Applies consistent system bar handling for edge-to-edge Android versions.
 * Root backgrounds remain visible behind the status/navigation bars while
 * screen content is padded away from those bars.
 */
public final class SystemBarInsets {

    private SystemBarInsets() {
    }

    public static void apply(@NonNull Activity activity) {
        if (shouldSkip(activity)) {
            return;
        }

        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        boolean useLightSystemBars = !isDarkSystemBarScreen(activity);
        WindowCompat.getInsetsController(window, window.getDecorView())
                .setAppearanceLightStatusBars(useLightSystemBars);
        WindowCompat.getInsetsController(window, window.getDecorView())
                .setAppearanceLightNavigationBars(useLightSystemBars);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        ViewGroup content = window.findViewById(android.R.id.content);
        content.post(() -> {
            if (content.getChildCount() == 0) {
                return;
            }

            View root = content.getChildAt(0);
            PaddingState rootPadding = new PaddingState(root);
            boolean hasFullscreenTopContent = hasFullscreenTopContent(activity);
            PaddingState toolbarPadding = getPaddingState(root, R.id.toolbar);
            MarginState floatingBackMargin = hasFullscreenTopContent
                    ? getMarginState(root, R.id.btnBack)
                    : null;
            PaddingState bottomNavigationPadding = getPaddingState(root, R.id.bottomNavigation);
            PaddingState bottomBarPadding = getPaddingState(root, R.id.bottomBar);
            MarginState continueButtonMargin = getMarginState(root, R.id.btnContinue);
            MarginState bottomLinksMargin = getMarginState(root, R.id.llBottomLinks);

            ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
                Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                                | WindowInsetsCompat.Type.displayCutout()
                );

                if (hasFullscreenTopContent) {
                    rootPadding.apply(systemBars.left, 0, systemBars.right, 0);
                    applyTopInsetToToolbar(toolbarPadding, systemBars.top);
                    applyTopInsetToMargin(floatingBackMargin, systemBars.top);
                } else {
                    rootPadding.apply(systemBars.left, systemBars.top, systemBars.right, 0);
                }

                applyBottomInsetToBar(bottomNavigationPadding, systemBars.bottom);
                applyBottomInsetToBar(bottomBarPadding, systemBars.bottom);
                applyBottomInsetToMargin(continueButtonMargin, systemBars.bottom);
                applyBottomInsetToMargin(bottomLinksMargin, systemBars.bottom);

                return insets;
            });
            ViewCompat.requestApplyInsets(root);
        });
    }

    private static boolean shouldSkip(Activity activity) {
        String className = activity.getClass().getName();
        return className.equals("com.bloom.MainActivity")
                || className.endsWith(".ui.home.HomeActivity")
                || className.endsWith(".ui.splash.SplashActivity")
                || className.endsWith(".ui.product.ProductDetailActivity")
                || className.endsWith(".ui.location.ManualLocationActivity")
                || className.endsWith(".ui.auth.ForgotPasswordActivity");
    }

    private static boolean isDarkSystemBarScreen(Activity activity) {
        // LuxFragment is now hosted inside HomeActivity which handles its own status bar color.
        return false;
    }

    private static boolean hasFullscreenTopContent(Activity activity) {
        String className = activity.getClass().getName();
        return className.endsWith(".ui.checkout.AddAddressActivity")
                || className.endsWith(".ui.product.ProductDetailActivity")
                || className.endsWith(".ui.shop.ShopDetailActivity")
                || className.endsWith(".ui.auth.OtpVerifyActivity")
                || className.endsWith(".ui.auth.SignupActivity");
    }

    private static PaddingState getPaddingState(View root, int viewId) {
        View target = root.findViewById(viewId);
        return target == null ? null : new PaddingState(target);
    }

    private static MarginState getMarginState(View root, int viewId) {
        View target = root.findViewById(viewId);
        if (target == null || !(target.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            return null;
        }
        return new MarginState(target);
    }

    private static void applyTopInsetToToolbar(PaddingState paddingState, int topInset) {
        if (paddingState == null) {
            return;
        }
        paddingState.apply(0, topInset, 0, 0);
        paddingState.applyExtraHeight(topInset);
    }

    private static void applyBottomInsetToBar(PaddingState paddingState, int bottomInset) {
        if (paddingState == null) {
            return;
        }
        paddingState.apply(0, 0, 0, bottomInset);
        paddingState.applyExtraHeight(bottomInset);
    }

    private static void applyTopInsetToMargin(MarginState marginState, int topInset) {
        if (marginState == null) {
            return;
        }
        marginState.apply(0, topInset, 0, 0);
    }

    private static void applyBottomInsetToMargin(MarginState marginState, int bottomInset) {
        if (marginState == null) {
            return;
        }
        marginState.apply(0, 0, 0, bottomInset);
    }

    private static final class PaddingState {
        private final View view;
        private final int initialLeft;
        private final int initialTop;
        private final int initialRight;
        private final int initialBottom;
        private final int initialHeight;

        private PaddingState(View view) {
            this.view = view;
            initialLeft = view.getPaddingLeft();
            initialTop = view.getPaddingTop();
            initialRight = view.getPaddingRight();
            initialBottom = view.getPaddingBottom();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            initialHeight = layoutParams == null ? 0 : layoutParams.height;
        }

        private void apply(int extraLeft, int extraTop, int extraRight, int extraBottom) {
            view.setPadding(
                    initialLeft + extraLeft,
                    initialTop + extraTop,
                    initialRight + extraRight,
                    initialBottom + extraBottom
            );
        }

        private void applyExtraHeight(int extraHeight) {
            if (initialHeight <= 0) {
                return;
            }
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = initialHeight + extraHeight;
            view.setLayoutParams(layoutParams);
        }
    }

    private static final class MarginState {
        private final View view;
        private final int initialLeft;
        private final int initialTop;
        private final int initialRight;
        private final int initialBottom;

        private MarginState(View view) {
            this.view = view;
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            initialLeft = layoutParams.leftMargin;
            initialTop = layoutParams.topMargin;
            initialRight = layoutParams.rightMargin;
            initialBottom = layoutParams.bottomMargin;
        }

        private void apply(int extraLeft, int extraTop, int extraRight, int extraBottom) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.setMargins(
                    initialLeft + extraLeft,
                    initialTop + extraTop,
                    initialRight + extraRight,
                    initialBottom + extraBottom
            );
            view.setLayoutParams(layoutParams);
        }
    }
}
