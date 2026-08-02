package com.bloom.customer.ui.common;

import android.app.Activity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bloom.customer.ui.home.HomeActivity;

/**
 * Utility for Fragments hosted inside {@link HomeActivity}.
 *
 * Since HomeActivity uses a transparent status bar (edge-to-edge), each
 * Fragment is responsible for padding its own topBar/toolbar down by the
 * status bar height. This makes the fragment's background colour fill the
 * transparent status bar area seamlessly while keeping content below it.
 *
 * Usage in Fragment.onViewCreated():
 * <pre>
 *   FragmentStatusBar.applyTopInset(this, binding.topBar);
 * </pre>
 */
public final class FragmentStatusBar {

    private FragmentStatusBar() {}

    /**
     * Applies status-bar-height as paddingTop to {@code topBarView}.
     *
     * If the host Activity is a {@link HomeActivity}, the already-captured
     * inset value is used immediately (no listener needed). Otherwise, a
     * {@link ViewCompat} inset listener is attached as a fallback.
     *
     * The original XML paddingTop of the view is preserved and the inset
     * is added on top of it, so calling this multiple times is safe.
     *
     * @param fragment   the calling fragment (used to resolve the Activity)
     * @param topBarView the view whose paddingTop should absorb the status bar
     */
    public static void applyTopInset(Fragment fragment, View topBarView) {
        if (topBarView == null) return;

        Activity activity = fragment.getActivity();
        if (activity instanceof HomeActivity) {
            HomeActivity home = (HomeActivity) activity;
            int statusBarHeight = home.getStatusBarHeight();
            if (statusBarHeight > 0) {
                // Insets already measured — apply immediately
                addTopPadding(topBarView, statusBarHeight);
                return;
            }
        }

        // Fallback: attach a one-shot listener for when insets are first dispatched
        final int[] originalTop = {topBarView.getPaddingTop()};
        final boolean[] applied = {false};

        ViewCompat.setOnApplyWindowInsetsListener(topBarView, (v, windowInsets) -> {
            if (!applied[0]) {
                Insets insets = windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                );
                if (insets.top > 0) {
                    v.setPadding(
                            v.getPaddingLeft(),
                            originalTop[0] + insets.top,
                            v.getPaddingRight(),
                            v.getPaddingBottom()
                    );
                    applied[0] = true;
                }
            }
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(topBarView);
    }

    private static void addTopPadding(View view, int extra) {
        // Use the raw XML value as baseline to avoid compounding calls on reuse
        Integer tag = (Integer) view.getTag(com.bloom.R.id.tag_original_top_padding);
        int originalTop;
        if (tag == null) {
            originalTop = view.getPaddingTop();
            view.setTag(com.bloom.R.id.tag_original_top_padding, originalTop);
        } else {
            originalTop = tag;
        }
        view.setPadding(
                view.getPaddingLeft(),
                originalTop + extra,
                view.getPaddingRight(),
                view.getPaddingBottom()
        );
    }
}
